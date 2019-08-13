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

class TGGFilesGenerator {
	static def String generateTGGFile(TripleGrammar pTGG) {
		val flattenedRules = pTGG.rules.map[EMSLFlattener.flatten(it) as TripleRule]
		
		// TODO add correct import statement -> extract resource location from metamodel reference?
		
		val allMetamodels = pTGG.srcMetamodels
		allMetamodels.addAll(pTGG.trgMetamodels)
		val resourceImports = allMetamodels.map[it.eResource.URI].stream.distinct.collect(Collectors.toList())
		
		'''
			«FOR uri : resourceImports»
				import "«uri»"
			«ENDFOR»
		
			«FOR rule : flattenedRules»
				«compileRule(rule)»
			«ENDFOR»
		'''
	}
	
	private static def compileRule(TripleRule pRule) {
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
	
	private static def compileModelNodeBlock(ModelNodeBlock pNodeBlock) {
		// TODO how to deal with multiple types that share names?
		
		'''
			«IF pNodeBlock.action?.op == ActionOperator.CREATE»++ «ENDIF»«pNodeBlock.name»:«pNodeBlock.type.name» {
				«FOR relation : pNodeBlock.relations»
					«compileRelationStatement(relation)»
				«ENDFOR»
				«FOR property : pNodeBlock.properties»
					«compilePropertyStatement(property)»
				«ENDFOR»
			}
		'''
	}
	
	private static def compileRelationStatement(ModelRelationStatement pRelationStatement) {
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
	
	private static def compilePropertyStatement(ModelPropertyStatement pPropertyStatement) {
		'''
			.«pPropertyStatement.type.name» «pPropertyStatement.op.literal» «resolveValue(pPropertyStatement.value)»
		'''
	}
	
	private static def resolveValue(Value pValue) {
		if(pValue instanceof PrimitiveBoolean)
			return pValue.^true
		else if(pValue instanceof PrimitiveInt)
			return pValue.literal
	}
}