package org.emoflon.neo.neo4j.adapter.templates

import java.util.List
import org.eclipse.xtend.lib.annotations.Data

@Data class NodeCommand extends ElementCommand {
	val String name = CypherBuilder.nextName
	val List<String> labels

	new(List<NeoProp> properties, List<String> labels) {
		super(properties)
		this.labels = labels
	}

	def String node() {
		'''(«name»:«labels.join(":")» {«properties.join(", ")»})'''
	}

	def String match() {
		'''MATCH «node»'''
	}
}
