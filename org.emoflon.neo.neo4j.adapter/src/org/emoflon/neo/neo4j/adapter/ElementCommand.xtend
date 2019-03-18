package org.emoflon.neo.neo4j.adapter

import java.util.List
import org.eclipse.xtend.lib.annotations.Data

@Data abstract class ElementCommand {
	val List<NeoProp> properties

	new(List<NeoProp> props) {
		if (props === null)
			throw new IllegalArgumentException("Properties should never be null!")

		properties = props
	}
}

@Data class NeoProp {
	val String key
	val String value

	override toString() '''«key» : «value»'''
}

@Data class NeoStrProp extends NeoProp {
	override toString() '''«key» : "«value»"'''
}
