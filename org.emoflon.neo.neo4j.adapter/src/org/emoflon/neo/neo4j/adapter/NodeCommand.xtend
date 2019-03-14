package org.emoflon.neo.neo4j.adapter

import java.util.List
import org.eclipse.xtend.lib.annotations.Data

@Data class NodeCommand extends ElementCommand {
	val List<String> labels
	val EdgeCommand typeOf
	val EdgeCommand elOf

	new(List<NeoProperty> properties, List<String> labels, NodeCommand type, NodeCommand container) {
		super(properties)
		this.labels = labels
		if (type !== null)
			typeOf = new EdgeCommand(List.of, NeoCoreBuilder.META_TYPE, this, type)
		else
			typeOf = null

		if (container !== null)
			elOf = new EdgeCommand(List.of, NeoCoreBuilder.META_EL_OF, this, container)
		else
			elOf = null
	}

	def match() {
		'''
			MATCH («id»:«labels.join(":")» {«properties.join(", ")»})
			«typeOf?.match»
			«elOf?.match»
		'''
	}

	def create() {
		'''
			CREATE («id»:«labels.join(":")» {«properties.join(", ")»})
			«typeOf?.create»
			«elOf?.create»
		'''
	}

	def merge() {
		'''
			MERGE («id»:«labels.join(":")» {«properties.join(", ")»})
			«typeOf?.merge»
			«elOf?.merge»
		'''
	}
}
