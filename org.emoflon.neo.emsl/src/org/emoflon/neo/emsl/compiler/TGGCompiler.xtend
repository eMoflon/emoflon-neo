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
import org.emoflon.neo.emsl.util.EMSLUtil
import org.emoflon.neo.emsl.eMSL.Parameter
import java.util.Map

class TGGCompiler {
	
	final String BASE_FOLDER = "../" + EMSLGenerator.TGG_GEN_FOLDER + "/";
	final String pathToGeneratedFiles
	TripleGrammar tgg
	BiMap<MetamodelNodeBlock, String> nodeTypeNames
	String importStatements

	new(TripleGrammar pTGG, String pathToGeneratedFiles) {
		tgg = pTGG
		this.pathToGeneratedFiles = pathToGeneratedFiles

		val allMetamodels = tgg.srcMetamodels + tgg.trgMetamodels
		buildImportStatement(allMetamodels)
		mapTypeNames(allMetamodels)
	}

	def void compileAll(IFileSystemAccess2 pFSA) {
		for (Operation operation : Operation.allOps) {
			val fileLocation = BASE_FOLDER + pathToGeneratedFiles + "/" + tgg.name + operation.nameExtension + ".msl"
			pFSA.generateFile(fileLocation, compile(operation))
		}
	}

	private def String compile(Operation pOp) {
		val flattenedRules = tgg.rules.map[EMSLFlattener.flatten(it) as TripleRule]
		'''
			«importStatements»
			
			grammar «tgg.name»_«pOp.nameExtension» {
				«FOR rule : flattenedRules»
					«rule.name»
				«ENDFOR»
			}
			
			«FOR rule : flattenedRules SEPARATOR "\n"»
				«compileRule(pOp, rule)»
			«ENDFOR»
		'''
	}

	private def mapTypeNames(Iterable<Metamodel> pMetamodels) {
		nodeTypeNames = HashBiMap.create()

		val nodeTypeToMetamodelName = new HashMap
		val relationTypeToNodeName = new HashMap
		for (Metamodel metamodel : pMetamodels)
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

	private def buildImportStatement(Iterable<Metamodel> pMetamodels) {
		val resourcesToImport = pMetamodels.map[it.eResource.URI].toSet
		importStatements = '''
			«FOR uri : resourcesToImport»
				import "«uri»"
			«ENDFOR»
		'''
	}

	private def compileRule(Operation op, TripleRule pRule) {

		val paramsToValues = new HashMap<Parameter, String>
		val paramsToDomain = new HashMap<Parameter, Boolean>
		val paramGroups = new HashMap<String, Collection<Parameter>>
		val paramsToProperty = new HashMap<Parameter, String>
		collectParameters(pRule.srcNodeBlocks, paramsToValues, paramsToDomain, true, paramGroups, paramsToProperty)
		collectParameters(pRule.trgNodeBlocks, paramsToValues, paramsToDomain, false, paramGroups, paramsToProperty)
		op.handleParameters(paramsToValues, paramsToProperty, paramsToDomain, paramGroups)
		
		val srcToCorr = new HashMap<ModelNodeBlock, Set<Correspondence>>()
		for (Correspondence corr : pRule.correspondences) {
			if (!srcToCorr.containsKey(corr.source))
				srcToCorr.put(corr.source, new HashSet())
			srcToCorr.get(corr.source).add(corr)
		}
		
		'''
			rule «pRule.name» {
				«FOR srcBlock : pRule.srcNodeBlocks SEPARATOR "\n"»
					«compileModelNodeBlock(op, srcBlock, srcToCorr.getOrDefault(srcBlock, Collections.emptySet), true, paramsToValues)»
				«ENDFOR»
			
				«FOR trgBlock : pRule.trgNodeBlocks SEPARATOR "\n"»
					«compileModelNodeBlock(op, trgBlock, Collections.emptySet, false, paramsToValues)»
				«ENDFOR»
			}
		'''
	}
	
	private def collectParameters(Collection<ModelNodeBlock> nodeBlocks,
									Map<Parameter, String> paramsToValues,
									Map<Parameter, Boolean> paramsToDomain,
									boolean domain,
									Map<String, Collection<Parameter>> paramGroups,
									Map<Parameter, String> paramsToProperty
	) {
		for(nodeBlock : nodeBlocks)
			for(prop : nodeBlock.properties)
				if(prop.value instanceof Parameter) {
					val param = prop.value as Parameter
					paramsToValues.put(param, '''<«param.name»>''')
					paramsToDomain.put(param, domain)
					paramsToProperty.put(param, '''«nodeBlock.name»::«prop.type.name»''')
					if(!paramGroups.containsKey(param.name))
						paramGroups.put(param.name, new HashSet)
					paramGroups.get(param.name).add(param)
				}
	}

	private def compileModelNodeBlock(Operation pOp, ModelNodeBlock pNodeBlock, Collection<Correspondence> pCorrs, boolean pIsSrc, Map<Parameter, String> paramsToValues) {
		'''
			«pOp.getAction(pNodeBlock.action, pIsSrc)»«pNodeBlock.name»:«nodeTypeNames.get(pNodeBlock.type)» {
				«FOR relation : pNodeBlock.relations»
					«compileRelationStatement(pOp, relation, pIsSrc, paramsToValues)»
				«ENDFOR»
				«FOR corr : pCorrs»
					«compileCorrespondence(pOp, corr)»
				«ENDFOR»
				«FOR property : pNodeBlock.properties»
					«compilePropertyStatement(pOp, property, pIsSrc, paramsToValues)»
				«ENDFOR»
				«pOp.getTranslation(pNodeBlock.action, pIsSrc)»
			}
		'''
	}

	private def compileRelationStatement(Operation pOp, ModelRelationStatement pRelationStatement, boolean pIsSrc, Map<Parameter, String> paramsToValues) {
		val translate = pOp.getTranslation(pRelationStatement.action, pIsSrc)
		val hasProperties = pRelationStatement.properties !== null && !pRelationStatement.properties.empty
		'''
			«pOp.getAction(pRelationStatement.action, pIsSrc)»-«compileRelationTypes(pRelationStatement.types)»->«pRelationStatement.target.name»
			«IF !translate.empty || hasProperties»
				{
					«IF hasProperties»
						«FOR property : pRelationStatement.properties»
							«compilePropertyStatement(pOp, property, pIsSrc, paramsToValues)»
						«ENDFOR»
					«ENDIF»
					«translate»
				}
			«ENDIF»
		'''
	}

	private def compileRelationTypes(List<ModelRelationStatementType> pTypes) {
		var types = ""
		for (ModelRelationStatementType type : pTypes) {
			if (types !== "")
				types += "|"
			types += type.type.name
		}
		return types
	}

	private def compileCorrespondence(Operation pOp, Correspondence pCorrespondence) {
		pOp.compileCorrespondence(pCorrespondence)
	}

	private def compilePropertyStatement(Operation op, ModelPropertyStatement pPropertyStatement, boolean isSrc, Map<Parameter, String> paramsToValues) {
		if(pPropertyStatement.value instanceof Parameter) {
			val param = pPropertyStatement.value as Parameter
			if(paramsToValues.get(param) === null) ""
			else '''.«pPropertyStatement.type.name» «op.getConditionOperator(pPropertyStatement.op, isSrc)» «paramsToValues.get(param)»'''
		} else '''.«pPropertyStatement.type.name» «op.getConditionOperator(pPropertyStatement.op, isSrc)» «EMSLUtil.handleValue(pPropertyStatement.value)»''' // TODO replace handleValue with own method
	}
}
