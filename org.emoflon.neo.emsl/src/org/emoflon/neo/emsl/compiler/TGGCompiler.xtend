package org.emoflon.neo.emsl.compiler

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import java.util.Collection
import java.util.Collections
import java.util.HashMap
import java.util.HashSet
import java.util.List
import java.util.Set
import java.util.stream.Collectors
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
import org.emoflon.neo.emsl.refinement.EMSLFlattener
import org.emoflon.neo.emsl.util.EMSLUtil

class TGGCompiler {
	final String BASE_FOLDER = "tgg/";
	TripleGrammar tgg
	BiMap<MetamodelNodeBlock, String> nodeTypeNames
	String importStatements

	new(TripleGrammar pTGG) {
		tgg = pTGG

		val allMetamodels = tgg.srcMetamodels
		allMetamodels.addAll(tgg.trgMetamodels)

		buildImportStatement(allMetamodels)

		mapTypeNames(allMetamodels)
	}

	def Collection<String> compileAll(IFileSystemAccess2 pFSA) {
		val generatedFiles = new HashSet<String>
		for (Operation operation : Operation.allOps) {
			val fileLocation = BASE_FOLDER + tgg.name + operation.nameExtension
			pFSA.generateFile(fileLocation, compile(operation))
			generatedFiles.add("src-gen/" + fileLocation)
		}
		return generatedFiles
	}

	private def String compile(Operation pOp) {
		'''
			«importStatements»
			
			«FOR rule : tgg.rules.map[EMSLFlattener.flatten(it) as TripleRule] SEPARATOR "\n"»
				«compileRule(pOp, rule)»
			«ENDFOR»
		'''
	}

	private def mapTypeNames(Collection<Metamodel> pMetamodels) {

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

	private def buildImportStatement(List<Metamodel> pMetamodels) {
		val resourcesToImport = pMetamodels.map[it.eResource.URI].stream.distinct.collect(Collectors.toSet())
		importStatements = '''
			«FOR uri : resourcesToImport»
				import "«uri»"
			«ENDFOR»
		'''
	}

	private def compileRule(Operation pOp, TripleRule pRule) {

		val srcToCorr = new HashMap<ModelNodeBlock, Set<Correspondence>>()
		for (Correspondence corr : pRule.correspondences) {
			if (!srcToCorr.containsKey(corr.source))
				srcToCorr.put(corr.source, new HashSet())
			srcToCorr.get(corr.source).add(corr)
		}

		'''
			rule «pRule.name» {
				«FOR srcBlock : pRule.srcNodeBlocks SEPARATOR "\n"»
					«compileModelNodeBlock(pOp, srcBlock, srcToCorr.getOrDefault(srcBlock, Collections.emptySet), true)»
				«ENDFOR»
			
				«FOR trgBlock : pRule.trgNodeBlocks SEPARATOR "\n"»
					«compileModelNodeBlock(pOp, trgBlock, Collections.emptySet, false)»
				«ENDFOR»
			}
		'''
	}

	private def compileModelNodeBlock(Operation pOp, ModelNodeBlock pNodeBlock, Collection<Correspondence> pCorrs, boolean pIsSrc) {
		'''
			«pOp.getAction(pNodeBlock.action, pIsSrc)»«pNodeBlock.name»:«nodeTypeNames.get(pNodeBlock.type)» {
				«FOR relation : pNodeBlock.relations»
					«compileRelationStatement(pOp, relation, pIsSrc)»
				«ENDFOR»
				«FOR corr : pCorrs»
					«compileCorrespondence(pOp, corr)»
				«ENDFOR»
				«FOR property : pNodeBlock.properties»
					«compilePropertyStatement(property)»
				«ENDFOR»
				«pOp.getTranslation(pNodeBlock.action, pIsSrc)»
			}
		'''
	}

	private def compileRelationStatement(Operation pOp, ModelRelationStatement pRelationStatement, boolean pIsSrc) {
		val translate = pOp.getTranslation(pRelationStatement.action, pIsSrc)
		val hasProperties = pRelationStatement.properties !== null && !pRelationStatement.properties.empty
		'''
			«pOp.getAction(pRelationStatement.action, pIsSrc)»-«compileRelationTypes(pRelationStatement.types)»->«pRelationStatement.target.name»
			«IF !translate.empty || hasProperties»
				{
					«IF hasProperties»
						«FOR property : pRelationStatement.properties»
							«compilePropertyStatement(property)»
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
		'''
			«pOp.getCorrAction(pCorrespondence.action)»-corr->«pCorrespondence.target.name»
			{
				._type_ := "«pCorrespondence.type.name»"
			}
		'''
	}

	private def compilePropertyStatement(ModelPropertyStatement pPropertyStatement) {
		// TODO any reason why EMSLUtil::handleValue shouldn't be used here?
		'''
			.«pPropertyStatement.type.name» «pPropertyStatement.op.literal» «EMSLUtil.handleValue(pPropertyStatement.value)»
		'''
	}
}
