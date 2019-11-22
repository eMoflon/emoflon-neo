package org.emoflon.neo.cypher.models.templates

import org.eclipse.xtend.lib.annotations.Data

@Data class EdgeCommand extends ElementCommand {
	val String label
	val NodeCommand from
	val NodeCommand to

	def edge() {
		'''(«from.name»)-[:«label» {«properties.join(", ")»}]->(«to.name»)'''
	}

	def match() {
		'''MATCH («from.name»)-[:«label» {«properties.join(", ")»}]->(«to.name»)'''
	}
}
