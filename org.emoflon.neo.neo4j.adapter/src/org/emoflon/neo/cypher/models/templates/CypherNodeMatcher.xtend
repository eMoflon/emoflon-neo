package org.emoflon.neo.cypher.models.templates

import java.util.ArrayList
import java.util.List
import org.neo4j.driver.Driver
import org.neo4j.driver.Result

class CypherNodeMatcher extends CypherBuilder {
	List<NodeCommand> nodesToReturn;

	new() {
		nodesToReturn = new ArrayList
	}

	private def String buildCommand() {
		if (nodesToReturn.size == 0)
			throw new IllegalStateException("You have to return something!")

		''' 
			«nodesToMatch.values.map[n| n.match()].join("\n")»
			RETURN «FOR nc : nodesToReturn SEPARATOR ","»(«nc.name»)«ENDFOR»
		'''
	}

	def void returnWith(NodeCommand... ncs) {
		if (!nodesToMatch.values.containsAll(ncs))
			throw new IllegalStateException("You can only return what you have matched!")

		nodesToReturn.addAll(ncs)
	}

	def Result run(Driver driver) {
		val session = driver.session
		runCypherCommand(session, buildCommand)
	}
}
