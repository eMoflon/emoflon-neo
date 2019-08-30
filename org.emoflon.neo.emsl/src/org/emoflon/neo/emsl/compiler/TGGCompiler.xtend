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
import org.eclipse.core.resources.IProject
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.emoflon.neo.emsl.eMSL.ActionOperator
import org.emoflon.neo.emsl.eMSL.Correspondence
import org.emoflon.neo.emsl.eMSL.Metamodel
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock
import org.emoflon.neo.emsl.eMSL.MetamodelRelationStatement
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement
import org.emoflon.neo.emsl.eMSL.ModelRelationStatementType
import org.emoflon.neo.emsl.eMSL.PrimitiveBoolean
import org.emoflon.neo.emsl.eMSL.PrimitiveInt
import org.emoflon.neo.emsl.eMSL.TripleGrammar
import org.emoflon.neo.emsl.eMSL.TripleRule
import org.emoflon.neo.emsl.eMSL.Value
import org.emoflon.neo.emsl.refinement.EMSLFlattener

//FIXME[Mario] Extract logic for different Ops into separate classes that implement the same "Operation" interface
// This would greatly simplify the code here
class TGGCompiler {
	final String BASE_FOLDER = "tgg/";
	TripleGrammar tgg
	BiMap<MetamodelNodeBlock, String> nodeTypeNames
	BiMap<MetamodelRelationStatement, String> relationTypeNames
	String importStatements

	new(TripleGrammar pTGG) {
		tgg = pTGG

		val allMetamodels = tgg.srcMetamodels
		allMetamodels.addAll(tgg.trgMetamodels)

		buildImportStatement(allMetamodels)

		mapTypeNames(allMetamodels)
	}

	def compileAll(IFileSystemAccess2 pFSA, IProject pProject) {
		for (OperationType operationType : OperationType.values) {
			opType = operationType
			val fileLocation = BASE_FOLDER + tgg.name + opType.opNameExtension
			pFSA.generateFile(fileLocation, compile(opType))
			// FIXME[Mario] This doesn't work for me (on Mac OSX)
			// Perhaps move touch to afterGenerate in EMSLGenerator?
			pProject.findMember("src-gen/" + fileLocation).touch(null)
		}
	}

	OperationType opType

	private def String compile(OperationType pOpType) {
		'''
			«importStatements»
			
			«FOR rule : tgg.rules.map[EMSLFlattener.flatten(it) as TripleRule] SEPARATOR "\n"»
				«compileRule(rule)»
			«ENDFOR»
		'''
	}

	private def mapTypeNames(Collection<Metamodel> pMetamodels) {

		nodeTypeNames = HashBiMap.create()
		relationTypeNames = HashBiMap.create();

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

		duplicateNames.clear()
		for (MetamodelRelationStatement type : relationTypeToNodeName.keySet) {
			if (relationTypeNames.containsValue(type.name)) {
				val otherType = relationTypeNames.inverse.get(type.name)
				relationTypeNames.put(otherType, relationTypeToNodeName.get(otherType) + "." + otherType.name)
				duplicateNames.add(type.name)
			}

			if (duplicateNames.contains(type.name))
				relationTypeNames.put(type, relationTypeToNodeName.get(type) + "." + type.name)
			else
				relationTypeNames.put(type, type.name)
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

	//FIXME[Mario]  I think the distinction between isTranslated (tr : true) and translate (tr := true) is missing
	// In the TGGs I looked at, I only found := even in places where it should be clearly :
	// There are basically two checks:
	// (1) Is something already translated? ==> tr : true
	// (2) Translate something but only if it is not already translated ==> tr : false  
	// 																		tr := true
	// 	   Note that you need both statements!
	private def compileRule(TripleRule pRule) {

		val srcToCorr = new HashMap<ModelNodeBlock, Set<Correspondence>>()
		for (Correspondence corr : pRule.correspondences) {
			if (!srcToCorr.containsKey(corr.source))
				srcToCorr.put(corr.source, new HashSet())
			srcToCorr.get(corr.source).add(corr)
		}

		'''
			rule «pRule.name» {
				«FOR srcBlock : pRule.srcNodeBlocks SEPARATOR "\n"»
					«compileModelNodeBlock(srcBlock, srcToCorr.getOrDefault(srcBlock, Collections.emptySet), true)»
				«ENDFOR»
			
				«FOR trgBlock : pRule.trgNodeBlocks SEPARATOR "\n"»
					«compileModelNodeBlock(trgBlock, Collections.emptySet, false)»
				«ENDFOR»
			}
		'''
	}

	private def compileModelNodeBlock(ModelNodeBlock pNodeBlock, Collection<Correspondence> pCorrs, boolean pIsSrc) {
		val translate = (opType === OperationType.FWD && pIsSrc) || (opType === OperationType.BWD && !pIsSrc)
		var action = ""
		if (pNodeBlock.action?.op === ActionOperator.CREATE &&
			(opType === OperationType.MODELGEN || (opType === OperationType.FWD && !pIsSrc) ||
				(opType === OperationType.BWD && pIsSrc)
			))
			action = "++ "
		'''
			«action»«pNodeBlock.name»:«nodeTypeNames.get(pNodeBlock.type)» {
				«FOR relation : pNodeBlock.relations»
					«compileRelationStatement(relation, pIsSrc)»
				«ENDFOR»
				«FOR corr : pCorrs»
					«compileCorrespondence(corr)»
				«ENDFOR»
				«FOR property : pNodeBlock.properties»
					«compilePropertyStatement(property)»
				«ENDFOR»
				«IF translate»
					._tr_ := true
				«ENDIF»
			}
		'''
	}

	private def compileRelationStatement(ModelRelationStatement pRelationStatement, boolean pIsSrc) {
		val translate = (opType === OperationType.FWD && pIsSrc) || (opType === OperationType.BWD && !pIsSrc)
		val hasProperties = pRelationStatement.properties !== null && !pRelationStatement.properties.empty
		var action = ""
		if (pRelationStatement.action?.op === ActionOperator.CREATE &&
			(opType === OperationType.MODELGEN || (opType === OperationType.FWD && !pIsSrc) ||
				(opType === OperationType.BWD && pIsSrc)
			))
			action = "++ "
		'''
			«action»-«compileRelationTypes(pRelationStatement.types)»->«pRelationStatement.target.name»
			«IF translate || hasProperties»
				{
					«IF hasProperties»
						«FOR property : pRelationStatement.properties»
							«compilePropertyStatement(property)»
						«ENDFOR»
					«ENDIF»
					«IF translate»
						~_tr_ := true
					«ENDIF»
				}
			«ENDIF»
		'''
	}

	private def compileRelationTypes(List<ModelRelationStatementType> pTypes) {
		var types = ""
		for (ModelRelationStatementType type : pTypes) {
			if (types !== "")
				types += "|"
			// FIXME[Mario]  I don't think duplicates should be handled
			// types += relationTypeNames.get(type.type)
			types += type.type.name
		}
		return types
	}

	private def compileCorrespondence(Correspondence pCorrespondence) {
		val action = (pCorrespondence.action?.op === ActionOperator.CREATE && opType !== OperationType.CO) ? "++ " : ""
		'''
			«action»-corr->«pCorrespondence.target.name»
			{
				._type_ := "«pCorrespondence.type.name»"
			}
		'''
	}

	private def compilePropertyStatement(ModelPropertyStatement pPropertyStatement) {
		'''
			.«pPropertyStatement.type.name» «pPropertyStatement.op.literal» «resolveValue(pPropertyStatement.value)»
		'''
	}

	private def resolveValue(Value pValue) {
		if (pValue instanceof PrimitiveBoolean)
			return pValue.^true
		else if (pValue instanceof PrimitiveInt)
			return pValue.literal
	}
}
