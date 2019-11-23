package org.emoflon.neo.cypher.models.templates

import java.util.HashMap
import java.util.List
import java.util.Map
import org.neo4j.driver.v1.Session
import org.neo4j.driver.v1.StatementResult
import org.apache.log4j.Logger
import org.emoflon.neo.neocore.util.NeoCoreConstants

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
			MATCH ()-[r]->() where id(r) = abs(eltID) delete r
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
			«matchAllNodesInModel(modelName, "n")»
			SET n._tr_ = false
		'''
	}

	def static String matchAllNodesInModel(String modelName, String node) {
		'''
			MATCH («node»)-[:elementOf]->(m:NeoCore__Model {ename: "«modelName»"})
		'''
	}

	def static String prepareTranslateAttributeForEdges(String modelName) {
		'''			
			«matchAllEdgesInModel(modelName, "r")»
			SET r._tr_ = false
		'''
	}

	def static String matchAllEdgesInModel(String modelName, String relation) {
		'''
			MATCH 
				(m:NeoCore__Model {ename: "«modelName»"}), 
				(a)-[:elementOf]->(m), 
				(b)-[:elementOf]->(m),
				(a)-[«relation»]->(b)
		'''
	}
	
	def static String getAllElOfEdgesInModel(String modelName){
		'''
			MATCH 
				(m:NeoCore__Model {ename: "«modelName»"}), 
				(a)-[r:elementOf]->(m)
			RETURN id(r)
		'''
	}

	def static String removeTranslationAttributeForNodes(String modelName) {
		'''
			«matchAllNodesInModel(modelName, "n")»
			remove n._tr_
		'''
	}

	def static String removeTranslationAttributeForEdges(String modelName) {
		'''
			«matchAllEdgesInModel(modelName, "r")»
			remove r._tr_
		'''
	}

	def static String getAllNodesInModel(String modelName) {
		'''
			«matchAllNodesInModel(modelName, "n")»
			RETURN DISTINCT id(n)
		'''
	}

	def static String getAllRelsInModel(String modelName) {
		'''
			«matchAllEdgesInModel(modelName, "r")»
			RETURN DISTINCT id(r)
		'''
	}

	def static String getAllCorrs(String src, String trg) {
		'''
			MATCH 
				(src:NeoCore__Model {ename: "«src»"}),
				(trg:NeoCore__Model {ename: "«trg»"}),
				(a)-[:«NeoCoreConstants.META_EL_OF»]->(src), 
				(b)-[:«NeoCoreConstants.META_EL_OF»]->(trg),
				(a)-[r:«NeoCoreConstants.CORR»]->(b)
			RETURN DISTINCT id(r)
		'''
	}
	
	def static String getModelNodes(String src, String trg){
		'''
			MATCH 
				(src:NeoCore__Model {ename: "«src»"}),
				(trg:NeoCore__Model {ename: "«trg»"})
			RETURN id(src), id(trg)
		'''
	}
	
	def static String getConformsToEdges(String model) {
		'''
			MATCH 
				(m:NeoCore__Model {ename: "«model»"}),
				(m)-[cts:«NeoCoreConstants.CONFORMS_TO_PROP»]->(mm)
			RETURN DISTINCT id(cts)
		'''
	}
}
