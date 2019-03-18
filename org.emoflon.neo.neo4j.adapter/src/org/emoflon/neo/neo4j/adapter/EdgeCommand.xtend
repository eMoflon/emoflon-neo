package org.emoflon.neo.neo4j.adapter

import org.eclipse.xtend.lib.annotations.Data

@Data class EdgeCommand extends ElementCommand {
	val String label
	val NodeCommand from
	val NodeCommand to

	def match() {
		'''MATCH («from.id»)-[:«label» {«properties.join(", ")»}]->(«to.id»)'''
	}

	def edge(){
		'''(«from.id»)-[:«label» {«properties.join(", ")»}]->(«to.id»)'''
	}

	def create() {
		'''CREATE «edge»'''
	}
}
