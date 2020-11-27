package org.emoflon.neo.cypher.models.templates

import java.util.HashMap
import java.util.List
import java.util.Map
import org.neo4j.driver.Session
import org.neo4j.driver.Result
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

	def Result runCypherCommand(Session session, String cmd) {
		logger.debug("-------- Begin Cypher ----")
		logger.debug(cmd)
		logger.debug("-------- End -------")

		return session.run(cmd)
	}

	def Result runCypherCommand(Session session, String cmd, Map<String, Object> params) {
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
			MATCH («node» {enamespace: "«modelName»"})
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
			MATCH (a {enamespace: "«modelName»"})-[«relation»]->(b {enamespace: "«modelName»"})
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
	
	def static String getAllNodesInDelta(String modelName, String delta) {
		'''
			«matchAllNodesInModel(modelName, "n")»
			WHERE n.«delta» = true
			RETURN DISTINCT id(n)
		'''	
	}
	
	def static String getAllEdgesInDelta(String modelName, String delta) {
		'''
			«matchAllEdgesInModel(modelName, "r")»
			WHERE r.«delta» = true
			RETURN DISTINCT id(r)
		'''	
	}
	
	def static String getAllCorrsInDelta(String src, String trg, String delta) {
		'''
			«matchAllCorrs(src, trg, "r")»
			WHERE r.«delta» = true
			RETURN DISTINCT id(r)
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
	
	def static String matchAllCorrs(String src, String trg, String relation) {
		'''	
			MATCH (a {enamespace: "«src»"})-[«relation»:«NeoCoreConstants.CORR»]->(b {enamespace: "«trg»"})	
		'''
	}
	
	def static String getAllCorrs(String src, String trg) {
		'''
			«matchAllCorrs(src, trg, "r")»
			RETURN DISTINCT id(r)
		'''
	}
	
	def static String prepareDeltaAttributeForNodes(String modelName, String delta) {
		'''			
			«matchAllNodesInModel(modelName, "n")»
			SET n.«delta» = true
		'''
	}
	
	def static String prepareDeltaAttributeForEdges(String modelName, String delta) {
		'''			
			«matchAllEdgesInModel(modelName, "r")»
			SET r.«delta» = true
		'''
	}
	
	def static String prepareDeltaAttributeForCorrs(String src, String trg, String delta) {
		'''			
			«matchAllCorrs(src, trg, "r")»
			SET r.«delta» = true
		'''
	}
	
	def static String removeDeltaAttributeForNodes(String modelName, String delta) {
		'''
			«matchAllNodesInModel(modelName, "n")»
			WHERE n.«delta» = true
			remove n.«delta»
			
		'''
	}

	def static String removeDeltaAttributeForEdges(String modelName, String delta) {
		'''
			«matchAllEdgesInModel(modelName, "r")»
			WHERE r.«delta» = true
			remove r.«delta»
			
		'''
	}
	
	def static String removeDeltaAttributeForCorrs(String src, String trg, String delta) {
		'''
			«matchAllCorrs(src, trg, "r")»
			WHERE r.«delta» = true
			remove r.«delta»
			
		'''
	}
}
