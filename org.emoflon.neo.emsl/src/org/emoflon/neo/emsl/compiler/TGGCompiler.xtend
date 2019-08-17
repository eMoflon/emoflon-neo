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
import java.util.Map

class TGGCompiler {
	Map<MetamodelNodeBlock, String> typeMap;
	
	def String compile(TripleGrammar pTGG) {
		val allMetamodels = pTGG.srcMetamodels
		allMetamodels.addAll(pTGG.trgMetamodels)
		
		typeMap = new HashMap();
		for(Metamodel metamodel : allMetamodels)
			for(MetamodelNodeBlock type : metamodel.nodeBlocks)
				typeMap.put(type, metamodel.name + type.name);
					
		'''
			«FOR uri : allMetamodels.map[it.eResource.URI].stream.distinct.collect(Collectors.toList())»
				import "«uri»"
			«ENDFOR»
		
			«FOR rule : pTGG.rules.map[EMSLFlattener.flatten(it) as TripleRule]»
				«compileRule(rule)»
			«ENDFOR»
		'''
	}
	
	private def compileRule(TripleRule pRule) {
		val types = pRule.srcNodeBlocks.map[it.type]
		types.addAll(pRule.trgNodeBlocks.map[it.type])
		
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