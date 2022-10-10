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
import java.util.Optional
import java.util.Set
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.emoflon.neo.emsl.compiler.Operation.Domain
import org.emoflon.neo.emsl.compiler.TGGCompilerUtils.ParameterDomain
import org.emoflon.neo.emsl.compiler.attributeConstraints.sorting.SearchPlanAction
import org.emoflon.neo.emsl.eMSL.ActionOperator
import org.emoflon.neo.emsl.eMSL.AtomicPattern
import org.emoflon.neo.emsl.eMSL.AttributeConstraint
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
import org.emoflon.neo.emsl.eMSL.ValueExpression
import org.emoflon.neo.emsl.generator.EMSLGenerator
import org.emoflon.neo.emsl.refinement.EMSLFlattener
import org.emoflon.neo.neocore.util.PreProcessorUtil

class TGGCompiler {
	final String BASE_FOLDER = EMSLGenerator.TGG_GEN_FOLDER + "/";
	final String apiRoot
	final String apiPath
	final String apiName
	TripleGrammar tgg
	List<TripleRule> flattenedRules
	BiMap<MetamodelNodeBlock, String> nodeTypeNames
	String importStatements
	
	HashMap<String, Map<Domain,ArrayList<String>>> ruleNameToGreenElements
	HashMap<String, ArrayList<Integer>> ruleNameToValidIDs

	new(TripleGrammar tgg, String apiRoot, String apiPath, String apiName) {
		this.tgg = tgg
		this.apiName = apiName
		this.apiPath = apiPath
		this.apiRoot = apiRoot

		val allMetamodels = new ArrayList
		allMetamodels.addAll(tgg.srcMetamodels) 
		allMetamodels.addAll(tgg.trgMetamodels)
		allMetamodels.add(PreProcessorUtil.instance.neoCore)
		mapTypeNames(allMetamodels)
		new HashMap();
		ruleNameToGreenElements = new HashMap();
		ruleNameToValidIDs = new HashMap();

		flattenedRules = tgg.rules.map[EMSLFlattener.flatten(it) as TripleRule]
		
		buildImportStatement(allMetamodels, flattenedRules)
	}

	def compileAll(IFileSystemAccess2 fsa) {
		val appGenerator = new TGGAppGenerator(tgg)
		var generatedFiles = new ArrayList<String>
		var pathToGeneratedFiles = '''«apiRoot»'''
		if(!apiPath.empty) pathToGeneratedFiles += "/" + apiPath
		for (Operation operation : Operation.allOps) {
			val prefix = apiPath.empty? "tgg/" : apiPath + "/tgg/"
			val ruleFileLocation = '''«BASE_FOLDER»«prefix»«tgg.name»«operation.nameExtension».msl'''
			// Important:  the actual file location must be without a "../"!
			fsa.generateFile("../" + ruleFileLocation, compile(operation))
			generatedFiles.add(ruleFileLocation)

			val appName = '''«tgg.name»«operation.nameExtension»_Run'''
			val appFileLocation = '''«BASE_FOLDER»«pathToGeneratedFiles»/run/«appName».java'''
			fsa.deleteFile("../" + appFileLocation);
			var packagePath = pathToGeneratedFiles.replace('/', '.')
			if(packagePath.startsWith('.')) packagePath = packagePath.substring(1)
			fsa.generateFile("../" + appFileLocation, appGenerator.generateApp(operation, packagePath, apiName, apiRoot.replace("/", ".")))
			generatedFiles.add(appFileLocation)
		}

		return generatedFiles
	}

	private def String compile(Operation op) {
		'''
			«importStatements»
			
			grammar «tgg.name»«op.nameExtension» {
				«ruleNameToGreenElements.clear»
				rules {
					«FOR rule : flattenedRules»
						«IF (op.multi)»
							«val greenElements = TGGCompilerValidations.getGreenElements(rule)»
							«val validIDs = new ArrayList<Integer>()»
							«ruleNameToGreenElements.put(rule.name, greenElements)»
						
							«FOR i : 0 ..< Math.pow(2,greenElements.get(Domain.SRC).size + greenElements.get(Domain.TRG).size + greenElements.get(Domain.CORR).size).intValue - 1 /*No off-by-one error, we don't want the GEN rule!*/»
								«IF TGGCompilerValidations.isValidRule(op, rule, i, ruleNameToGreenElements.get(rule.name))»
									«i > 0 ? rule.name + "_" + i : rule.name»
									«{validIDs.add(i) ""}»
								«ENDIF»
							«ENDFOR»
							«ruleNameToValidIDs.put(rule.name, validIDs)»
						«ELSE»	
							«rule.name»
						«ENDIF»
					«ENDFOR»
				}
			}
			
			«FOR rule : flattenedRules SEPARATOR "\n"»
				«IF (op.multi)»
					«FOR i : ruleNameToValidIDs.get(rule.name)»
						«compileRule(op, rule, true, i)»
					«ENDFOR»
				«ELSE»	
					«compileRule(op, rule, true, 0)»
				«ENDIF»
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

	private def buildImportStatement(Iterable<Metamodel> metamodels, Iterable<TripleRule> rules) {
		val resourcesToImport = (metamodels.filter[
			it.eResource !== null
		].map [
			it.eResource.URI
		] + rules.flatMap [
			it.attributeConstraints.map[it.type.eResource.URI]
		]).toSet
		
		importStatements = '''
			«FOR uri : resourcesToImport»
				import "«uri»"
			«ENDFOR»
		'''
	}

	private def compileRule(Operation op, TripleRule rule, boolean mapToModel, int ruleID) {

		val paramsToData = new HashMap<Parameter, ParameterData>
		val paramGroups = new HashMap<String, Collection<Parameter>>
		val nacPatterns = op.preprocessNACs(rule.nacs).toInvertedMap[EMSLFlattener.flatten(it.pattern) as AtomicPattern]

		collectParameters(rule.srcNodeBlocks, ParameterDomain.SRC, paramsToData, paramGroups)
		collectParameters(rule.trgNodeBlocks, ParameterDomain.TRG, paramsToData, paramGroups)
		collectParameters(nacPatterns.values.flatMap[it.nodeBlocks], ParameterDomain.NAC, paramsToData, paramGroups)
	
		if (op.multi)
			op.handleParameters(paramsToData, paramGroups, ruleID, ruleNameToGreenElements.get(rule.name))
		else
			op.handleParameters(paramsToData, paramGroups)
		
		val srcToCorr = new HashMap<ModelNodeBlock, Set<Correspondence>>()
		for (Correspondence corr : rule.correspondences) {
			if (!srcToCorr.containsKey(corr.source))
				srcToCorr.put(corr.source, new HashSet())
			srcToCorr.get(corr.source).add(corr)
		}

		'''
			  rule «op.multi && ruleID > 0 ? rule.name + "_" + ruleID : rule.name» {
				«FOR srcBlock : rule.srcNodeBlocks SEPARATOR "\n"»
					«compileModelNodeBlock(op, srcBlock, srcToCorr.getOrDefault(srcBlock, Collections.emptySet), Domain.SRC, paramsToData, mapToModel, rule, ruleID)»
				«ENDFOR»
			
				«FOR trgBlock : rule.trgNodeBlocks SEPARATOR "\n"»
					«compileModelNodeBlock(op, trgBlock, Collections.emptySet, Domain.TRG, paramsToData, mapToModel, rule, ruleID)»
				«ENDFOR»
				
				«compileAttributeConstraints(op, rule.attributeConstraints, paramsToData)»
			} «IF !nacPatterns.isEmpty»when «rule.name»NAC«ENDIF»
			
			«IF (nacPatterns.size === 1)»
				«val nacName = getNacName(rule, nacPatterns.values.head)»
				constraint «rule.name»NAC = forbid «nacName»
				
					«TGGCompilerUtils.printAtomicPattern(nacName, nacPatterns.values.head, nacPatterns.keySet.head instanceof SourceNAC, nodeTypeNames, paramsToData, mapToModel)»
			«ELSEIF (nacPatterns.size > 1)»
				constraint «rule.name»NAC = «FOR pattern : nacPatterns.values SEPARATOR ' && '»«getNacName(rule, pattern)»NAC«ENDFOR»
				
				«FOR nacPattern : nacPatterns.entrySet»
					«val nacName = getNacName(rule, nacPattern.value)»
					constraint «nacName»NAC = forbid «nacName»
					
						«TGGCompilerUtils.printAtomicPattern(nacName, nacPattern.value, nacPattern.key instanceof SourceNAC, nodeTypeNames, paramsToData, mapToModel)»
				«ENDFOR»
			«ENDIF»
		'''
	}
	
	private def compileAttributeConstraints(Operation op, List<AttributeConstraint> attributeConstraints, Map<Parameter, ParameterData> paramsToData) {
		val variables = attributeConstraints.flatMap[it.values].map[it.value].toSet.toList
		val searchPlan = new SearchPlanAction(variables, attributeConstraints, op, paramsToData)
		val sortedConstraints = searchPlan.sortConstraints

		'''
			attributeConstraints {
				«FOR attrConstr : sortedConstraints»
					«attrConstr.type.name»(
						«FOR assignment : attrConstr.values SEPARATOR","»
							«assignment.type.name»=«handleValue(assignment.value, paramsToData)»
						«ENDFOR»
					)
				«ENDFOR»
			}
		'''
	}

	static def String handleValue(ValueExpression value, Map<Parameter, ParameterData> paramsToData) {
		return getOperationalisedValue(value, paramsToData)//
			.orElseGet([TGGCompilerUtils.handleValue(value)])
	}

	private static def getOperationalisedValue(ValueExpression value, Map<Parameter, ParameterData> paramsToData) {
		if (value instanceof Parameter) {
			val parametersInRule = paramsToData.filter [ k, v |
				k.name.equals(value.name)
			].values

			if (parametersInRule.size > 0) {
				val parameterDataInRule = parametersInRule.get(0)
				return parameterDataInRule.boundValue
			}
		}
		return Optional.empty()
	}
	
	private def String getNacName(TripleRule rule, AtomicPattern pattern) {
		'''«rule.name»_«pattern.name»'''
	}

	private def collectParameters(Iterable<ModelNodeBlock> nodeBlocks, ParameterDomain domain,
		Map<Parameter, ParameterData> paramsToData, Map<String, Collection<Parameter>> paramGroups) {
		for (nodeBlock : nodeBlocks)
			for (prop : nodeBlock.properties)
				if (prop.value instanceof Parameter) {
					val param = prop.value as Parameter

					paramsToData.put(param, new ParameterData(param.name, domain, nodeBlock, prop.type.name))

					if (!paramGroups.containsKey(param.name))
						paramGroups.put(param.name, new HashSet)
					paramGroups.get(param.name).add(param)
				}
	}

	private def compileModelNodeBlock(Operation op, ModelNodeBlock nodeBlock, Collection<Correspondence> corrs,
		Domain domain, Map<Parameter, ParameterData> paramsToData, boolean mapToModel, TripleRule rule, int ruleID) {
		val action = op.multi ? op.getAction(nodeBlock.action, ruleID, ruleNameToGreenElements.get(rule.name),
				nodeBlock.name) : op.getAction(nodeBlock.action, domain)
		val comparison = action == "++"? ":=" : ":"
		'''
			«action»«nodeBlock.name»:«nodeTypeNames.get(nodeBlock.type)» {
				«IF mapToModel».enamespace «comparison» «IF domain.equals(Domain.SRC)»<__srcModelName>«ELSE»<__trgModelName>«ENDIF»«ENDIF»
				«FOR relation : nodeBlock.relations»
				«compileRelationStatement(op, relation, domain, paramsToData, rule, ruleID)»
				«ENDFOR»
				«FOR corr : corrs»
				«compileCorrespondence(op, corr, rule, ruleID)»
				«ENDFOR»
				«FOR property : nodeBlock.properties»
				«compilePropertyStatement(op, property, domain, paramsToData, rule, ruleID, nodeBlock.name)»
				«ENDFOR»
				«op.getTranslation(nodeBlock.action, domain)»
				«IF flattenedRules.stream.anyMatch[name.equals(rule.name)]»
					«op.getDeltaCondition(nodeBlock.action, ruleID, ruleNameToGreenElements.get(rule.name), nodeBlock.name)»
				«ENDIF»
			}
		'''
	}

	private def compileRelationStatement(Operation op, ModelRelationStatement relationStatement, Domain domain,
		Map<Parameter, ParameterData> paramsToData, TripleRule rule, int ruleID) {
		val translate = op.getTranslation(relationStatement.action, domain)
		val hasProperties = relationStatement.properties !== null && !relationStatement.properties.empty
		val delta = flattenedRules.stream.anyMatch[name.equals(rule.name)] ? op.getDeltaCondition(relationStatement.action, ruleID, ruleNameToGreenElements.get(rule.name),
				(relationStatement.eContainer as ModelNodeBlock).name + "->" + relationStatement.target.name) : ""
		val action = op.multi ? op.getAction(relationStatement.action, ruleID, ruleNameToGreenElements.get(rule.name),
				(relationStatement.eContainer as ModelNodeBlock).name + "->" + relationStatement.target.name) : op.
				getAction(relationStatement.action, domain)
		'''
			«action»-«compileRelationTypes(relationStatement.types)»->«relationStatement.target.name»
			«IF !translate.empty || hasProperties || !delta.empty»
				{
					«IF hasProperties»
						«FOR property : relationStatement.properties»
							«compilePropertyStatement(op, property, domain, paramsToData, rule, ruleID, 
								(relationStatement.eContainer as ModelNodeBlock).name + "->" + relationStatement.target.name)»
						«ENDFOR»
					«ENDIF»
					«translate»
					«delta»
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

	private def compileCorrespondence(Operation op, Correspondence corr, TripleRule rule, int ruleID) {
		val action = op.multi ? op.getAction(corr.action, ruleID, ruleNameToGreenElements.get(rule.name),
				corr.source.name + "->" + corr.target.name) : op.getAction(corr.action, Domain.CORR)
		'''
			«action»-corr->«corr.target.name»
			{
				._type_ «IF ActionOperator.CREATE.toString.equals(action)»:=«ELSE»:«ENDIF» "«corr.type.name»"
			}
		'''
	}

	private def compilePropertyStatement(Operation op, ModelPropertyStatement propertyStatement, Domain domain,
		Map<Parameter, ParameterData> paramsToData, TripleRule rule, int ruleID, String blockName) {
		if (propertyStatement.value instanceof Parameter) {
			val paramValue = paramsToData.get(propertyStatement.value as Parameter).printValue
			if (!paramValue.present)
				""
			else
				if (op.multi)
					'''.«propertyStatement.type.name» «op.getConditionOperator(propertyStatement.op, ruleID, ruleNameToGreenElements.get(rule.name), blockName)» «paramValue.get»'''
				else
					'''.«propertyStatement.type.name» «op.getConditionOperator(propertyStatement.op, domain)» «paramValue.get»'''
		} else
				if (op.multi)
					'''.«propertyStatement.type.name» «op.getConditionOperator(propertyStatement.op, ruleID, ruleNameToGreenElements.get(rule.name), blockName)» «TGGCompilerUtils.handleValue(propertyStatement.value)»'''
				else
					'''.«propertyStatement.type.name» «op.getConditionOperator(propertyStatement.op, domain)» «TGGCompilerUtils.handleValue(propertyStatement.value)»'''
	}
}
