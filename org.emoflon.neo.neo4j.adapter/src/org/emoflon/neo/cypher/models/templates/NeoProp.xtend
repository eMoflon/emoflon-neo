package org.emoflon.neo.cypher.models.templates
import org.eclipse.xtend.lib.annotations.Data

@Data class NeoProp {
	val String key
	val Object value

	override toString() '''
		«key» : «IF value instanceof String»"«value»"«ELSE»«value»«ENDIF»
	'''
}