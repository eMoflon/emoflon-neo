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

		val paramsToValues = new HashMap<Parameter, String>
		val paramsToDomain = new HashMap<Parameter, Boolean>
		val paramGroups = new HashMap<String, Collection<Parameter>>
		val paramsToProperty = new HashMap<Parameter, String>
		collectParameters(rule.srcNodeBlocks, paramsToValues, paramsToDomain, true, paramGroups, paramsToProperty)
		collectParameters(rule.trgNodeBlocks, paramsToValues, paramsToDomain, false, paramGroups, paramsToProperty)
		op.handleParameters(paramsToValues, paramsToProperty, paramsToDomain, paramGroups)
		
		val srcToCorr = new HashMap<ModelNodeBlock, Set<Correspondence>>()
		for (Correspondence corr : rule.correspondences) {
			if (!srcToCorr.containsKey(corr.source))
				srcToCorr.put(corr.source, new HashSet())
			srcToCorr.get(corr.source).add(corr)
		}
		
		val nacs = op.compileNACs(rule.nacs)
		
		'''
			rule «rule.name» {
				«FOR srcBlock : rule.srcNodeBlocks SEPARATOR "\n"»
					«compileModelNodeBlock(op, srcBlock, srcToCorr.getOrDefault(srcBlock, Collections.emptySet), true, paramsToValues)»
				«ENDFOR»
			
				«FOR trgBlock : rule.trgNodeBlocks SEPARATOR "\n"»
					«compileModelNodeBlock(op, trgBlock, Collections.emptySet, false, paramsToValues)»
				«ENDFOR»
			} «IF !nacs.isEmpty»when «rule.name»NAC«ENDIF»
			
			«compileNACs(rule.name, nacs)»
		'''
	}
	
	private def compileNACs(String ruleName, Collection<AtomicPattern> nacs) {
		if(nacs.isEmpty)
			""
		else if(nacs.size === 1) {
			val nac = nacs.head
			'''
				constraint «ruleName»NAC = forbid «nac.name»
				
				«TGGCompilerUtils.simplePrintAtomicPattern(nac, nodeTypeNames)»
			'''
		} else		
			'''
				constraint «ruleName»NAC = «FOR nac : nacs SEPARATOR '&&'»«nac.name»NAC«ENDFOR»
				
				«FOR nac : nacs»
					constraint «nac.name»NAC = forbid «nac.name»
				
					«TGGCompilerUtils.simplePrintAtomicPattern(nac, nodeTypeNames)»
				«ENDFOR»
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

	private def compileModelNodeBlock(Operation op, ModelNodeBlock nodeBlock, Collection<Correspondence> corrs, boolean isSrc, Map<Parameter, String> paramsToValues) {
		'''
			«op.getAction(nodeBlock.action, isSrc)»«nodeBlock.name»:«nodeTypeNames.get(nodeBlock.type)» {
				«FOR relation : nodeBlock.relations»
					«compileRelationStatement(op, relation, isSrc, paramsToValues)»
				«ENDFOR»
				«FOR corr : corrs»
					«compileCorrespondence(op, corr)»
				«ENDFOR»
				«FOR property : nodeBlock.properties»
					«compilePropertyStatement(op, property, isSrc, paramsToValues)»
				«ENDFOR»
				«op.getTranslation(nodeBlock.action, isSrc)»
			}
		'''
	}

	private def compileRelationStatement(Operation op, ModelRelationStatement relationStatement, boolean isSrc, Map<Parameter, String> paramsToValues) {
		val translate = op.getTranslation(relationStatement.action, isSrc)
		val hasProperties = relationStatement.properties !== null && !relationStatement.properties.empty
		'''
			«op.getAction(relationStatement.action, isSrc)»-«compileRelationTypes(relationStatement.types)»->«relationStatement.target.name»
			«IF !translate.empty || hasProperties»
				{
					«IF hasProperties»
						«FOR property : relationStatement.properties»
							«compilePropertyStatement(op, property, isSrc, paramsToValues)»
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

	private def compilePropertyStatement(Operation op, ModelPropertyStatement propertyStatement, boolean isSrc, Map<Parameter, String> paramsToValues) {
		if(propertyStatement.value instanceof Parameter) {
			val param = propertyStatement.value as Parameter
			if(paramsToValues.get(param) === null) ""
			else '''.«propertyStatement.type.name» «op.getConditionOperator(propertyStatement.op, isSrc)» «paramsToValues.get(param)»'''
		} else '''.«propertyStatement.type.name» «op.getConditionOperator(propertyStatement.op, isSrc)» «TGGCompilerUtils.handleValue(propertyStatement.value)»'''
	}
}
