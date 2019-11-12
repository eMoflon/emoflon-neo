package org.emoflon.neo.cypher.models.templates

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
			val nc = new NodeCommand(props, labels.subList(0, 1))
			nodesToMatch.put(key, nc)
			return nc
		}
	}

	private def String createKeyForNode(List<NeoProp> props, List<String> labels) {
		'''«props.join("-")»-«labels.join("-")»'''
	}

	def static String deleteEdgesQuery(String ids) {
		'''
			UNWIND $«ids» as eltID
			MATCH ()-[r]->() where id(r) = eltID delete r
		'''
	}

	def static String deleteNodesQuery(String ids) {
		'''
			UNWIND $«ids» as eltID
			MATCH (n) where id(n) = eltID detach delete n
		'''
	}

	def static String deleteEdgesOfType(String type) {
		'''
			MATCH ()-[r:«type»]->() delete r
		'''
	}

	def static String prepareTranslateAttributeForNodes(String modelName) {
		'''			
			«matchAllNodesInModel(modelName)»
			SET n._tr_ = false
		'''
	}
	
	def static String matchAllNodesInModel(String modelName){
		'''
			MATCH (n)-[:elementOf]-(m:NeoCore__Model {ename: "«modelName»"})
		'''
	}
	
	def static String prepareTranslateAttributeForEdges(String modelName) {
		'''			
			«matchAllEdgesInModel(modelName)»
			SET r._tr_ = false
		'''
	}
	
	def static String matchAllEdgesInModel(String modelName){
		'''
			MATCH 
				(m:NeoCore__Model {ename: "«modelName»"}), 
				(a)-[:elementOf]->(m), 
				(b)-[:elementOf]->(m),
				(a)-[r]-(b)
		'''
	}
	
	def static String removeTranslationAttributeForNodes(String modelName){
		'''
			«matchAllNodesInModel(modelName)»
			remove n._tr_
		'''
	}
	
	def static String removeTranslationAttributeForEdges(String modelName){
		'''
			«matchAllEdgesInModel(modelName)»
			remove r._tr_
		'''
	}
}
