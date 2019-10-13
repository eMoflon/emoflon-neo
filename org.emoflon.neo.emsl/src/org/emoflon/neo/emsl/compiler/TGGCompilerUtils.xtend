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

class TGGCompilerUtils {
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
}
