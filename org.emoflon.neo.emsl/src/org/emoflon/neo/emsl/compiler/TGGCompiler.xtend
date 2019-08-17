package org.emoflon.neo.emsl.compiler

import org.emoflon.neo.emsl.eMSL.TripleGrammar
import org.emoflon.neo.emsl.eMSL.ActionOperator
import org.emoflon.neo.emsl.refinement.EMSLFlattener
import org.emoflon.neo.emsl.eMSL.TripleRule
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement
import org.emoflon.neo.emsl.eMSL.Value
import org.emoflon.neo.emsl.eMSL.PrimitiveBoolean
import org.emoflon.neo.emsl.eMSL.PrimitiveInt
import java.util.stream.Collectors
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock
import org.emoflon.neo.emsl.eMSL.Metamodel
import java.util.HashMap
import java.util.Collection
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import java.util.HashSet

class TGGCompiler {
	BiMap<MetamodelNodeBlock, String> typeMap;
	
	def String compile(TripleGrammar pTGG) {
		val allMetamodels = pTGG.srcMetamodels
		allMetamodels.addAll(pTGG.trgMetamodels)

		mapTypeNames(allMetamodels)
					
		'''
			«FOR uri : allMetamodels.map[it.eResource.URI].stream.distinct.collect(Collectors.toList())»
				import "«uri»"
			«ENDFOR»
		
			«FOR rule : pTGG.rules.map[EMSLFlattener.flatten(it) as TripleRule]»
				«compileRule(rule)»
			«ENDFOR»
		'''
	}
	
	private def mapTypeNames(Collection<Metamodel> pMetamodels) {
		
		typeMap = HashBiMap.create()

		val allTypes = new HashMap
		for(Metamodel metamodel : pMetamodels)
			for(MetamodelNodeBlock type : metamodel.nodeBlocks)
				allTypes.put(type, metamodel);
		
		val duplicateNames = new HashSet
		for(MetamodelNodeBlock type : allTypes.keySet) {
			if(typeMap.containsValue(type.name)) {
				val otherType = typeMap.inverse.get(type.name)
				typeMap.put(otherType, allTypes.get(otherType).name + "." + otherType.name)
				duplicateNames.add(type.name)
			}
			
			if(duplicateNames.contains(type.name))
				typeMap.put(type, allTypes.get(type).name + "." + type.name)
			else
				typeMap.put(type, type.name)
		}
	}
	
	private def compileRule(TripleRule pRule) {
		
		'''
			rule «pRule.name» {
				«FOR srcBlock : pRule.srcNodeBlocks»
					«compileModelNodeBlock(srcBlock)»
				«ENDFOR»

				«FOR trgBlock : pRule.trgNodeBlocks»
					«compileModelNodeBlock(trgBlock)»
				«ENDFOR»
			}
		'''
	}
	
	private def compileModelNodeBlock(ModelNodeBlock pNodeBlock) {
		'''
			«IF pNodeBlock.action?.op == ActionOperator.CREATE»++ «ENDIF»«pNodeBlock.name»:«typeMap.get(pNodeBlock.type)» {
				«FOR relation : pNodeBlock.relations»
					«compileRelationStatement(relation)»
				«ENDFOR»
				«FOR property : pNodeBlock.properties»
					«compilePropertyStatement(property)»
				«ENDFOR»
			}
		'''
	}
	
	private def compileRelationStatement(ModelRelationStatement pRelationStatement) {
		'''
			«IF pRelationStatement.action?.op == ActionOperator.CREATE»++ «ENDIF»-«pRelationStatement.types.get(0).type.name»->«pRelationStatement.target.name»
			«IF pRelationStatement.properties !== null && !pRelationStatement.properties.empty»
			{
				«FOR property : pRelationStatement.properties»
					«compilePropertyStatement(property)»
				«ENDFOR»
			}
			«ENDIF»
		'''
	}
	
	private def compilePropertyStatement(ModelPropertyStatement pPropertyStatement) {
		'''
			.«pPropertyStatement.type.name» «pPropertyStatement.op.literal» «resolveValue(pPropertyStatement.value)»
		'''
	}
	
	private def resolveValue(Value pValue) {
		if(pValue instanceof PrimitiveBoolean)
			return pValue.^true
		else if(pValue instanceof PrimitiveInt)
			return pValue.literal
	}
}