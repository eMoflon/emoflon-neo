package org.emoflon.neo.neo4j.adapter

import java.util.List
import org.eclipse.xtend.lib.annotations.Data

@Data abstract class ElementCommand {
	val List<NeoProperty> properties

	new(List<NeoProperty> props) {
		if(props === null)
			throw new IllegalArgumentException("Properties should never be null!")
		
		properties = props
	}

	static int _id = 0
	val String id = "_" + _id++
}

@Data class NeoProperty {
	val String key
	val String value

	override toString() '''
		«key» : «value»
	'''
}

@Data class NeoStringProperty extends NeoProperty {
	override toString() '''
		«key» : "«value»"
	'''
}
