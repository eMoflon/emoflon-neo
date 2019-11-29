package org.emoflon.neo.emsl.compiler

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import java.util.ArrayList
import java.util.Collection
import java.util.Collections
import java.util.HashMap
import java.util.HashSet
import java.util.List
import java.util.Map
import java.util.Set
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.emoflon.neo.emsl.compiler.TGGCompilerUtils.ParameterDomain
import org.emoflon.neo.emsl.eMSL.AtomicPattern
import org.emoflon.neo.emsl.eMSL.Correspondence
import org.emoflon.neo.emsl.eMSL.Metamodel
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock
import org.emoflon.neo.emsl.eMSL.MetamodelRelationStatement
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement
import org.emoflon.neo.emsl.eMSL.ModelRelationStatementType
import org.emoflon.neo.emsl.eMSL.Parameter
import org.emoflon.neo.emsl.eMSL.SourceNAC
import org.emoflon.neo.emsl.eMSL.TripleGrammar
import org.emoflon.neo.emsl.eMSL.TripleRule
import org.emoflon.neo.emsl.generator.EMSLGenerator
import org.emoflon.neo.emsl.refinement.EMSLFlattener
import org.emoflon.neo.emsl.compiler.TGGCompilerUtils.ParameterDomain
import org.emoflon.neo.emsl.eMSL.EMSLFactory
import org.emoflon.neo.emsl.eMSL.ActionOperator
import org.emoflon.neo.emsl.eMSL.ConditionOperator
import org.emoflon.neo.neocore.util.PreProcessorUtil

class TGGCompiler {
	final String BASE_FOLDER = EMSLGenerator.TGG_GEN_FOLDER + "/";
	final String pathToGeneratedFiles
	TripleGrammar tgg
	List<TripleRule> flattenedRules
	List<TripleRule> generatedRules
	BiMap<MetamodelNodeBlock, String> nodeTypeNames
	String importStatements
	
	public final static String CREATE_SRC_MODEL_RULE = "createSrcModel"
	public final static String CREATE_TRG_MODEL_RULE = "createTrgModel"

	new(TripleGrammar tgg, String pathToGeneratedFiles) {
		this.tgg = tgg
		this.pathToGeneratedFiles = pathToGeneratedFiles

		val allMetamodels = new ArrayList
		allMetamodels.addAll(tgg.srcMetamodels)
		allMetamodels.addAll(tgg.trgMetamodels)
		buildImportStatement(allMetamodels)
		allMetamodels.add(PreProcessorUtil.instance.neoCore)
		mapTypeNames(allMetamodels)
		
		flattenedRules = tgg.rules.map[EMSLFlattener.flatten(it) as TripleRule]
		generatedRules = new ArrayList()
		generatedRules.add(generateSrcModelCreationRule)
		generatedRules.add(generateTrgModelCreationRule)
	}

	def compileAll(IFileSystemAccess2 fsa) {
		val appGenerator = new TGGAppGenerator(tgg)
		var generatedFiles = new ArrayList<String>
		for (Operation operation : Operation.allOps) {
			val ruleFileLocation = '''«BASE_FOLDER»«pathToGeneratedFiles»/«tgg.name»«operation.nameExtension».msl'''
			// Important:  the actual file location must be without a "../"!
			fsa.generateFile("../" + ruleFileLocation, compile(operation))
			generatedFiles.add(ruleFileLocation)
			
			val appName = '''«tgg.name»«operation.nameExtension»_Run'''
			val appFileLocation = '''«BASE_FOLDER»«pathToGeneratedFiles»/run/«appName».java'''
			fsa.deleteFile("../" + appFileLocation);
			var fullPackage = '''«pathToGeneratedFiles.replace('/','.')».run'''
			if(fullPackage.startsWith(".")) fullPackage = fullPackage.substring(1)
			fsa.generateFile("../" + appFileLocation, appGenerator.generateApp(operation, fullPackage))
			generatedFiles.add(appFileLocation)
		}
		
		return generatedFiles
	}

	private def String compile(Operation op) {
		'''
			«importStatements»
			
			grammar «tgg.name»«op.nameExtension» {
				«FOR rule : flattenedRules»
					«rule.name»
				«ENDFOR»
				«FOR rule : generatedRules»
					«rule.name»
				«ENDFOR»
			}
			
			«FOR rule : flattenedRules SEPARATOR "\n"»
				«compileRule(op, rule, true)»
			«ENDFOR»

			«FOR rule : generatedRules SEPARATOR "\n"»
				«compileRule(op, rule, false)»
			«ENDFOR»
		'''
	}

	private def mapTypeNames(Iterable<Metamodel> metamodels) {
		nodeTypeNames = HashBiMap.create()

		val nodeTypeToMetamodelName = new HashMap
		val relationTypeToNodeName = new HashMap
		for (Metamodel metamodel : metamodels)
			for (MetamodelNodeBlock nodeType : metamodel.nodeBlocks) {
				nodeTypeToMetamodelName.put(nodeType, metamodel.name)
				for (MetamodelRelationStatement relationType : nodeType.relations)
					relationTypeToNodeName.put(relationType, nodeType.name)
			}

		val duplicateNames = new HashSet
		for (MetamodelNodeBlock type : nodeTypeToMetamodelName.keySet) {
			if (nodeTypeNames.containsValue(type.name)) {
				val otherType = nodeTypeNames.inverse.get(type.name)
				nodeTypeNames.put(otherType, nodeTypeToMetamodelName.get(otherType) + "." + otherType.name)
				duplicateNames.add(type.name)
			}

			if (duplicateNames.contains(type.name))
				nodeTypeNames.put(type, nodeTypeToMetamodelName.get(type) + "." + type.name)
			else
				nodeTypeNames.put(type, type.name)
		}
	}

	private def buildImportStatement(Iterable<Metamodel> metamodels) {
		val resourcesToImport = metamodels.map[it.eResource.URI].toSet
		importStatements = '''
			«FOR uri : resourcesToImport»
				import "«uri»"
			«ENDFOR»
		'''
	}

	private def compileRule(Operation op, TripleRule rule, boolean mapToModel) {

		val paramsToData = new HashMap<Parameter, ParameterData>
		val paramGroups = new HashMap<String, Collection<Parameter>>
		val nacPatterns = op.preprocessNACs(rule.nacs).toInvertedMap[EMSLFlattener.flatten(it.pattern) as AtomicPattern]
		
  		collectParameters(rule.srcNodeBlocks, ParameterDomain.SRC, paramsToData, paramGroups)
		collectParameters(rule.trgNodeBlocks, ParameterDomain.TRG, paramsToData, paramGroups)
		collectParameters(nacPatterns.values.flatMap[it.nodeBlocks], ParameterDomain.NAC, paramsToData, paramGroups)
		
		op.handleParameters(paramsToData, paramGroups)
		
		val srcToCorr = new HashMap<ModelNodeBlock, Set<Correspondence>>()
		for (Correspondence corr : rule.correspondences) {
			if (!srcToCorr.containsKey(corr.source))
				srcToCorr.put(corr.source, new HashSet())
			srcToCorr.get(corr.source).add(corr)
		}
		
		
		'''
			rule «rule.name» {
				«IF mapToModel && !rule.srcNodeBlocks.isEmpty»
					srcM : Model {
						.ename : <__srcModelName>
					}
					
				«ENDIF»
				«IF mapToModel && !rule.trgNodeBlocks.isEmpty»
					trgM : Model {
						.ename : <__trgModelName>
					}
					
				«ENDIF»
				«FOR srcBlock : rule.srcNodeBlocks SEPARATOR "\n"»
					«compileModelNodeBlock(op, srcBlock, srcToCorr.getOrDefault(srcBlock, Collections.emptySet), true, paramsToData, mapToModel)»
				«ENDFOR»
			
				«FOR trgBlock : rule.trgNodeBlocks SEPARATOR "\n"»
					«compileModelNodeBlock(op, trgBlock, Collections.emptySet, false, paramsToData, mapToModel)»
				«ENDFOR»
			} «IF !nacPatterns.isEmpty»when «rule.name»NAC«ENDIF»
			
			«IF(nacPatterns.size === 1)»
				«val nacName = getNacName(rule, nacPatterns.values.head)»
				constraint «rule.name»NAC = forbid «nacName»

				«TGGCompilerUtils.printAtomicPattern(nacName, nacPatterns.values.head, nacPatterns.keySet.head instanceof SourceNAC, nodeTypeNames, paramsToData, mapToModel)»
			«ELSEIF(nacPatterns.size > 1)»
					constraint «rule.name»NAC = «FOR pattern : nacPatterns.values SEPARATOR ' && '»«getNacName(rule, pattern)»NAC«ENDFOR»
					
					«FOR nacPattern : nacPatterns.entrySet»
						«val nacName = getNacName(rule, nacPattern.value)»
						constraint «nacName»NAC = forbid «nacName»
					
						«TGGCompilerUtils.printAtomicPattern(nacName, nacPattern.value, nacPattern.key instanceof SourceNAC, nodeTypeNames, paramsToData, mapToModel)»
					«ENDFOR»
			«ENDIF»
		'''
	}
	
	private def String getNacName(TripleRule rule, AtomicPattern pattern) {
		'''«rule.name»_«pattern.name»'''
	}
	
	private def collectParameters(Iterable<ModelNodeBlock> nodeBlocks,
									ParameterDomain domain,
									Map<Parameter, ParameterData> paramsToData,
									Map<String, Collection<Parameter>> paramGroups) {
		for(nodeBlock : nodeBlocks)
			for(prop : nodeBlock.properties)
				if(prop.value instanceof Parameter) {
					val param = prop.value as Parameter
					
					paramsToData.put(param, new ParameterData(param.name, domain, nodeBlock, prop.type.name))
					
					if(!paramGroups.containsKey(param.name))
						paramGroups.put(param.name, new HashSet)
					paramGroups.get(param.name).add(param)
				}
	}

	private def compileModelNodeBlock(Operation op, ModelNodeBlock nodeBlock, Collection<Correspondence> corrs, boolean isSrc, Map<Parameter, ParameterData> paramsToData, boolean mapToModel) {
		val action = op.getAction(nodeBlock.action, isSrc)
		'''
			«action»«nodeBlock.name»:«nodeTypeNames.get(nodeBlock.type)» {
				«IF mapToModel»«action»-elementOf->«IF isSrc»srcM«ELSE»trgM«ENDIF»«ENDIF»
				«FOR relation : nodeBlock.relations»
					«compileRelationStatement(op, relation, isSrc, paramsToData)»
				«ENDFOR»
				«FOR corr : corrs»
					«compileCorrespondence(op, corr)»
				«ENDFOR»
				«FOR property : nodeBlock.properties»
					«compilePropertyStatement(op, property, isSrc, paramsToData)»
				«ENDFOR»
				«op.getTranslation(nodeBlock.action, isSrc)»
			}
		'''
	}

	private def compileRelationStatement(Operation op, ModelRelationStatement relationStatement, boolean isSrc, Map<Parameter, ParameterData> paramsToData) {
		val translate = op.getTranslation(relationStatement.action, isSrc)
		val hasProperties = relationStatement.properties !== null && !relationStatement.properties.empty
		'''
			«op.getAction(relationStatement.action, isSrc)»-«compileRelationTypes(relationStatement.types)»->«relationStatement.target.name»
			«IF !translate.empty || hasProperties»
				{
					«IF hasProperties»
						«FOR property : relationStatement.properties»
							«compilePropertyStatement(op, property, isSrc, paramsToData)»
						«ENDFOR»
					«ENDIF»
					«translate»
				}
			«ENDIF»
		'''
	}

	private def compileRelationTypes(List<ModelRelationStatementType> types) {
		var typeString = ""
		for (ModelRelationStatementType type : types) {
			if (typeString !== "")
				typeString += "|"
			typeString += type.type.name
		}
		return typeString
	}

	private def compileCorrespondence(Operation op, Correspondence correspondence) {
		op.compileCorrespondence(correspondence)
	}

	private def compilePropertyStatement(Operation op, ModelPropertyStatement propertyStatement, boolean isSrc, Map<Parameter, ParameterData> paramsToData) {
		if(propertyStatement.value instanceof Parameter) {
			val paramValue = paramsToData.get(propertyStatement.value as Parameter).printValue
			if(!paramValue.present)
				""
			else
				'''.«propertyStatement.type.name» «op.getConditionOperator(propertyStatement.op, isSrc)» «paramValue.get»'''
		}
		else
			'''.«propertyStatement.type.name» «op.getConditionOperator(propertyStatement.op, isSrc)» «TGGCompilerUtils.handleValue(propertyStatement.value)»'''
	}

	/*
	 * model creation rule generation
	 */
	private def generateSrcModelCreationRule() {
		val modelBlockName = "srcModel"
		val modelNameParam = "__srcModelName"
		val metamodelBlocks = generateMetamodelBlocks(tgg.srcMetamodels.map[it.name])
		
		val sourceNAC = EMSLFactory.eINSTANCE.createSourceNAC
		sourceNAC.pattern = generateModelPattern(modelBlockName, modelNameParam)
		
		val modelCreationRule = EMSLFactory.eINSTANCE.createTripleRule
		modelCreationRule.name = CREATE_SRC_MODEL_RULE
		modelCreationRule.type = tgg
		modelCreationRule.nacs.add(sourceNAC)
		modelCreationRule.srcNodeBlocks.add(generateModelBlock(modelBlockName, modelNameParam, metamodelBlocks))
		for(ModelNodeBlock block : metamodelBlocks)
			modelCreationRule.srcNodeBlocks.add(block)
			
		return modelCreationRule
	}
	
	private def generateTrgModelCreationRule() {
		val modelBlockName = "trgModel"
		val modelNameParam = "__trgModelName"
		val metamodelBlocks = generateMetamodelBlocks(tgg.trgMetamodels.map[it.name])
		
		val targetNAC = EMSLFactory.eINSTANCE.createTargetNAC
		targetNAC.pattern = generateModelPattern(modelBlockName, modelNameParam)
		
		val modelCreationRule = EMSLFactory.eINSTANCE.createTripleRule
		modelCreationRule.name = CREATE_TRG_MODEL_RULE
		modelCreationRule.type = tgg
		modelCreationRule.nacs.add(targetNAC)
		modelCreationRule.trgNodeBlocks.add(generateModelBlock(modelBlockName, modelNameParam, metamodelBlocks))
		for(ModelNodeBlock block : metamodelBlocks)
			modelCreationRule.trgNodeBlocks.add(block)
			
		return modelCreationRule
	}
	
	private def generateModelBlock(String modelBlockName, String modelNameParam, List<ModelNodeBlock> metamodelBlocks) {
		val modelName = EMSLFactory.eINSTANCE.createModelPropertyStatement
		modelName.type = PreProcessorUtil.instance.ename
		modelName.op = ConditionOperator.ASSIGN
		modelName.value = generateParameter(modelNameParam)
		
		val modelBlock = EMSLFactory.eINSTANCE.createModelNodeBlock
		modelBlock.action = generateCreateAction
		modelBlock.name = modelBlockName
		modelBlock.type = PreProcessorUtil.instance.model
		modelBlock.properties.add(modelName)
		for(ModelNodeBlock block : metamodelBlocks)
			modelBlock.relations.add(generateConformsToEdge(block))
			
		return modelBlock
	}
	
	private def generateModelPattern(String modelBlockName, String modelNameParam) {
		val patternModelName = EMSLFactory.eINSTANCE.createModelPropertyStatement
		patternModelName.type = PreProcessorUtil.instance.ename
		patternModelName.op = ConditionOperator.EQ
		patternModelName.value = generateParameter(modelNameParam)
		
		val patternModelBlock = EMSLFactory.eINSTANCE.createModelNodeBlock
		patternModelBlock.name = modelBlockName
		patternModelBlock.type = PreProcessorUtil.instance.model
		patternModelBlock.properties.add(patternModelName)
		
		val modelPattern = EMSLFactory.eINSTANCE.createAtomicPattern
		modelPattern.name = '''«modelBlockName»Exists'''
		modelPattern.nodeBlocks.add(patternModelBlock)
		
		return modelPattern
	}
	
	private def generateMetamodelBlocks(List<String> metamodelNames) {
		val metamodelBlocks = new ArrayList
		
		for(String name : metamodelNames) {
			val nameString = EMSLFactory.eINSTANCE.createPrimitiveString
			nameString.literal = name
			
			val nameProperty = EMSLFactory.eINSTANCE.createModelPropertyStatement
			nameProperty.type = PreProcessorUtil.instance.ename
			nameProperty.op = ConditionOperator.EQ
			nameProperty.value = nameString
			
			val nodeBlock = EMSLFactory.eINSTANCE.createModelNodeBlock
			nodeBlock.name = '''mm«name»'''
			nodeBlock.type = PreProcessorUtil.instance.metaModel
			nodeBlock.properties.add(nameProperty)
			
			metamodelBlocks.add(nodeBlock)
		}
		
		return metamodelBlocks
	}
	
	private def generateConformsToEdge(ModelNodeBlock metamodelNodeBlock) {
		val conformsToEdgeType = EMSLFactory.eINSTANCE.createModelRelationStatementType
		conformsToEdgeType.type = PreProcessorUtil.instance.conformsTo

		val conformsToEdge = EMSLFactory.eINSTANCE.createModelRelationStatement
		conformsToEdge.action = generateCreateAction
		conformsToEdge.target = metamodelNodeBlock
		conformsToEdge.types.add(conformsToEdgeType)
		
		return conformsToEdge
	}
	
	private def generateParameter(String name) {
		val param = EMSLFactory.eINSTANCE.createParameter
		param.name = name
		return param
	}

	private def generateCreateAction() {
		val action = EMSLFactory.eINSTANCE.createAction
		action.op = ActionOperator.CREATE
		return action
	}
}
