package org.emoflon.neo.emsl.compiler

import org.emoflon.neo.emsl.eMSL.Parameter
import org.emoflon.neo.emsl.eMSL.ValueExpression
import org.emoflon.neo.emsl.eMSL.PrimitiveString
import org.emoflon.neo.emsl.eMSL.PrimitiveInt
import org.emoflon.neo.emsl.eMSL.PrimitiveBoolean
import org.emoflon.neo.emsl.eMSL.EnumValue
import org.emoflon.neo.emsl.eMSL.AttributeExpression
import org.emoflon.neo.emsl.eMSL.NodeAttributeExpTarget
import org.emoflon.neo.emsl.eMSL.LinkAttributeExpTarget
import org.emoflon.neo.emsl.eMSL.BinaryExpression
import org.emoflon.neo.emsl.eMSL.AtomicPattern
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock
import com.google.common.collect.BiMap

class TGGCompilerUtils {
	enum ParameterDomain {
		SRC, TRG, NAC
	}
	
	def static String handleValue(ValueExpression value) {
		if(value instanceof PrimitiveString) return '''"«value.literal»"'''

		if(value instanceof PrimitiveInt) return '''«value.literal»'''

		if(value instanceof PrimitiveBoolean) return '''«value.^true»'''

		if(value instanceof EnumValue) return '''"«value.literal.name»"'''

		if (value instanceof AttributeExpression) {
			// node::<target>
			val node = value.node
			val target = value.target

			if (target instanceof NodeAttributeExpTarget) // node::attribute
				return '''«node.name»::«target.attribute.name»'''
			else if (target instanceof LinkAttributeExpTarget) // node::-link->target::attribute
				return '''«node.name»::-«target.link.name»->«target.target.name»::«target.attribute.name»'''
		}
		
		if(value instanceof BinaryExpression)
			return '''«handleValue(value.left)»«value.op»«handleValue(value.right)»'''
		
		if(value instanceof Parameter){
			return '''<«value.name»>'''
		}

		throw new IllegalArgumentException('''Not yet able to handle: «value»''')
	}

	def static String simplePrintAtomicPattern(AtomicPattern pattern, BiMap<MetamodelNodeBlock, String> nodeTypeNames) {
		'''
			pattern «pattern.name» {
				«FOR nodeBlock : pattern.nodeBlocks»
					«simplePrintNodeBlock(nodeBlock, nodeTypeNames)»
				«ENDFOR»
			}
		'''
	}
	
	def static String simplePrintNodeBlock(ModelNodeBlock nodeBlock, BiMap<MetamodelNodeBlock, String> nodeTypeNames) {
		val nodeTypeName = if(nodeTypeNames.containsKey(nodeBlock.type)) nodeTypeNames.get(nodeBlock.type) else nodeBlock.type.name
		'''
			«nodeBlock.name» : «nodeTypeName» {
				«FOR relation : nodeBlock.relations»
					«simplePrintRelationStatement(relation)»
				«ENDFOR»
				«FOR property : nodeBlock.properties»
					«simplePrintPropertyStatement(property)»
				«ENDFOR»
			}
		'''
	}
	
	def static String simplePrintRelationStatement(ModelRelationStatement relationStatement) {
		'''
			-«FOR type : relationStatement.types SEPARATOR '|'»«type.type.name»«ENDFOR»->«relationStatement.target.name»
			«IF  relationStatement.properties !== null && !relationStatement.properties.empty»
				{
					«FOR property : relationStatement.properties»
						«simplePrintPropertyStatement(property)»
					«ENDFOR»
				}
			«ENDIF»
		'''
	}
	
	def static String simplePrintPropertyStatement(ModelPropertyStatement propertyStatement) {
		'''.«propertyStatement.type.name» «propertyStatement.op» «handleValue(propertyStatement.value)»'''
	}
}
