package org.emoflon.neo.neo4j.adapter

import java.util.ArrayList
import java.util.List
import org.eclipse.xtend.lib.annotations.Data

@Data class NodeCommand extends ElementCommand {
	val String id = "_" + CypherBuilder.nextId
	val List<String> labels

	new(List<NeoProp> properties, List<String> labels) {
		super(properties)
		this.labels = labels
	}

	def String node(){
		'''(«id»:«labels.join(":")» {«properties.join(", ")»})'''
	}

	def String match() {
		'''MATCH «node»'''
	}

	// Add pending label and temp id
	def create() {
		var allLabels = new ArrayList
		allLabels.add(CypherCreator.PENDING_LABEL)
		allLabels.addAll(labels)

		var allProps = new ArrayList
		allProps.add(new NeoStrProp(CypherCreator.TEMP_ID_PROP, id))
		allProps.addAll(properties)

		'''CREATE («id»:«allLabels.join(":")» {«allProps.join(", ")»})'''
	}

	def getId() {
		id
	}
}
