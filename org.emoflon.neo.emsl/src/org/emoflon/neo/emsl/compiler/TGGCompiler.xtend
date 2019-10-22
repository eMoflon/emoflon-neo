package org.emoflon.neo.emsl.compiler

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import java.util.Collection
import java.util.Collections
import java.util.HashMap
import java.util.HashSet
import java.util.List
import java.util.Set
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.emoflon.neo.emsl.eMSL.Correspondence
import org.emoflon.neo.emsl.eMSL.Metamodel
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock
import org.emoflon.neo.emsl.eMSL.MetamodelRelationStatement
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement
import org.emoflon.neo.emsl.eMSL.ModelRelationStatementType
import org.emoflon.neo.emsl.eMSL.TripleGrammar
import org.emoflon.neo.emsl.eMSL.TripleRule
import org.emoflon.neo.emsl.generator.EMSLGenerator
import org.emoflon.neo.emsl.refinement.EMSLFlattener
import org.emoflon.neo.emsl.eMSL.Parameter
import java.util.Map
import org.emoflon.neo.emsl.compiler.TGGCompilerUtils.ParameterDomain
import org.emoflon.neo.emsl.eMSL.AtomicPattern

class TGGCompiler {
	
	final String BASE_FOLDER = "../" + EMSLGenerator.TGG_GEN_FOLDER + "/";
	final String pathToGeneratedFiles
	TripleGrammar tgg
	BiMap<MetamodelNodeBlock, String> nodeTypeNames
	String importStatements

	new(TripleGrammar tgg, String pathToGeneratedFiles) {
		this.tgg = tgg
		this.pathToGeneratedFiles = pathToGeneratedFiles

		val allMetamodels = tgg.srcMetamodels + tgg.trgMetamodels
		buildImportStatement(allMetamodels)
		mapTypeNames(allMetamodels)
	}

	def void compileAll(IFileSystemAccess2 fsa) {
		for (Operation operation : Operation.allOps) {
			val fileLocation = BASE_FOLDER + pathToGeneratedFiles + "/" + tgg.name + operation.nameExtension + ".msl"
			fsa.generateFile(fileLocation, compile(operation))
		}
	}

	private def String compile(Operation op) {
		val flattenedRules = tgg.rules.map[EMSLFlattener.flatten(it) as TripleRule]
		'''
			«importStatements»
			
			grammar «tgg.name»_«op.nameExtension» {
				«FOR rule : flattenedRules»
					«rule.name»
				«ENDFOR»
			}
			
			«FOR rule : flattenedRules SEPARATOR "\n"»
				«compileRule(op, rule)»
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

	private def compileRule(Operation op, TripleRule rule) {

		val paramsToData = new HashMap<Parameter, ParameterData>
		val paramGroups = new HashMap<String, Collection<Parameter>>
		
		collectParameters(rule.srcNodeBlocks, ParameterDomain.SRC, paramsToData, paramGroups)
		collectParameters(rule.trgNodeBlocks, ParameterDomain.TRG, paramsToData, paramGroups)
		collectParameters(rule.nacs.flatMap[it.pattern.nodeBlocks], ParameterDomain.NAC, paramsToData, paramGroups)
		
		op.handleParameters(paramsToData, paramGroups)
		
		val srcToCorr = new HashMap<ModelNodeBlock, Set<Correspondence>>()
		for (Correspondence corr : rule.correspondences) {
			if (!srcToCorr.containsKey(corr.source))
				srcToCorr.put(corr.source, new HashSet())
			srcToCorr.get(corr.source).add(corr)
		}
		
		val nacPatterns = op.preprocessNACs(rule.nacs).map[EMSLFlattener.flatten(it.pattern) as AtomicPattern]
		
		'''
			rule «rule.name» {
				«FOR srcBlock : rule.srcNodeBlocks SEPARATOR "\n"»
					«compileModelNodeBlock(op, srcBlock, srcToCorr.getOrDefault(srcBlock, Collections.emptySet), true, paramsToData)»
				«ENDFOR»
			
				«FOR trgBlock : rule.trgNodeBlocks SEPARATOR "\n"»
					«compileModelNodeBlock(op, trgBlock, Collections.emptySet, false, paramsToData)»
				«ENDFOR»
			} «IF !nacPatterns.isEmpty»when «rule.name»NAC«ENDIF»
			
			«IF(nacPatterns.size === 1)»
				«val nac = nacPatterns.head»
				constraint «rule.name»NAC = forbid «nac.name»

				«TGGCompilerUtils.printAtomicPattern(nac, nodeTypeNames, paramsToData)»
			«ELSEIF(nacPatterns.size > 1)»
					constraint «rule.name»NAC = «FOR nac : nacPatterns SEPARATOR '&&'»«nac.name»NAC«ENDFOR»
					
					«FOR nac : nacPatterns»
						constraint «nac.name»NAC = forbid «nac.name»
					
						«TGGCompilerUtils.printAtomicPattern(nac, nodeTypeNames, paramsToData)»
					«ENDFOR»
			«ENDIF»
		'''
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

	private def compileModelNodeBlock(Operation op, ModelNodeBlock nodeBlock, Collection<Correspondence> corrs, boolean isSrc, Map<Parameter, ParameterData> paramsToData) {
		'''
			«op.getAction(nodeBlock.action, isSrc)»«nodeBlock.name»:«nodeTypeNames.get(nodeBlock.type)» {
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
			if(paramValue === null)
				""
			else
				'''.«propertyStatement.type.name» «op.getConditionOperator(propertyStatement.op, isSrc)» «paramValue»'''
		}
		else
			'''.«propertyStatement.type.name» «op.getConditionOperator(propertyStatement.op, isSrc)» «TGGCompilerUtils.handleValue(propertyStatement.value)»'''
	}
}
