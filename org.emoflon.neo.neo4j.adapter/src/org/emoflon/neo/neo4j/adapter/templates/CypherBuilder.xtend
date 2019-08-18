package org.emoflon.neo.neo4j.adapter.templates

import java.util.HashMap
import java.util.List
import java.util.Map
import org.neo4j.driver.v1.Session
import org.neo4j.driver.v1.StatementResult
import org.apache.log4j.Logger

abstract class CypherBuilder {
	static Logger logger = Logger.getLogger(CypherBuilder)
	static long COUNTER;

	static def final String nextName() {
		return "_" + COUNTER++;
	}
	
	protected Map<String, NodeCommand> nodesToMatch

	new() {
		CypherBuilder.COUNTER = 0
		nodesToMatch = new HashMap
	}

	def StatementResult runCypherCommand(Session session, String cmd) {
		logger.debug("-------- Begin Cypher ----")
		logger.debug(cmd)
		logger.debug("-------- End -------")
		
		return session.run(cmd)
	}
	
	def StatementResult runCypherCommand(Session session, String cmd, Map<String, Object> params) {
		logger.debug("-------- Begin Cypher ----")
		logger.debug(cmd)
		logger.debug("-------- End -------")
		
		return session.run(cmd, params)
	}
	
	def matchNode(List<NeoProp> props, List<String> labels) {
		val key = createKeyForNode(props, labels)
		if (nodesToMatch.containsKey(key))
			return nodesToMatch.get(key)
		else {
			val nc = new NodeCommand(props, labels)
			nodesToMatch.put(key, nc)
			return nc
		}
	}

	def String createKeyForNode(List<NeoProp> props, List<String> labels) {
		'''«props.join("-")»-«labels.join("-")»'''
	}
}
