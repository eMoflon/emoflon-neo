package org.emoflon.neo.neo4j.adapter

import java.util.Collection
import java.util.UUID

class CypherPatternBuilder {
	
	def static String createCypherQuery(Collection<NeoNode> nodes, Collection<NeoCondition> conditions, Collection<NeoRelation> relations) {
		val mnName = "matchingNode"
		val relName = "matches"
		cypherMatch(nodes, relations) + cypherConditions(conditions) + cypherCreate(nodes,mnName,relName) + cypherReturn(mnName);
	}
	
	
	def static String cypherMatch(Collection<NeoNode> nodes, Collection<NeoRelation> relations) {

		'''MATCH  «cypherNodes(nodes)»«IF relations.size > 0 », «ENDIF»«cypherRelations(relations)»
		''' 
	}
	
	
	def static String cypherNodes(Collection<NeoNode> nodes) {
		'''«FOR n:nodes SEPARATOR ', '»«n.toString»«ENDFOR»'''
	}
	
	def static String cypherNode(String varName, String classType, Collection<NeoProperty> properties) {
		'''(«varName»:«classType»«IF properties.size > 0» «cypherProperties(properties)»«ENDIF»)'''
	}
	
	def static String cypherRelations(Collection<NeoRelation> relations) {
		'''«IF relations.size > 0»
		«FOR r:relations SEPARATOR', '»«r.toString»«ENDFOR»
		«ENDIF»'''
	}
	
	def static String cypherRelation(NeoNode node, NeoRelation relation, Collection<NeoProperty> properties) {
		'''(«node.varName»)-[«relation.relName»:«relation.relType»«IF properties.size > 0» «cypherProperties(properties)»«ENDIF»]->(«relation.toVarName»)'''
	}

	
	def static String cypherConditions(Collection<NeoCondition> conditions) {
		'''«IF conditions.size > 0»
		WHERE «FOR c:conditions SEPARATOR ' AND '»«c.toString»«ENDFOR»«ENDIF»'''
	}
	
	def static String cypherCondition(String name, String op, Boolean opNeg, String value, String classVarName) {
		'''«IF opNeg»NOT «ENDIF»«classVarName».«name» «op» «value»'''
	}
	
	def static String cypherProperties(Collection<NeoProperty> properties) {
		'''«IF properties.size > 0»«FOR p:properties BEFORE'{' SEPARATOR', ' AFTER '}'»«p.toString»«ENDFOR»«ENDIF»'''
	}
	
	def static String cypherProperty(String name, String value, String classVarName) {
		'''«name»: «value»'''
	}
	
	def static String cypherPropertyClassTyped(String name, String value, String classVarName) {
		'''«classVarName».«name»: «value»'''
	}
	
	def static cypherCreate(Collection<NeoNode> nodes, String mnName, String relName) {
		'''CREATE («mnName»:Match), «cypherMatchNodeRelations(nodes, mnName, relName)» '''
	}
	
	def static cypherMatchNodeRelations(Collection<NeoNode> nodes, String mnName, String relName) {
		'''«FOR n:nodes SEPARATOR ', '»«cypherMatchNodeRelation(n,mnName,relName)»«ENDFOR»'''
	}
	
	def static cypherMatchNodeRelation(NeoNode node, String mnName, String relName) {
		'''(«mnName»)-[:«relName»]->(«node.varName»)'''
	}
	
	def static String cypherReturn(String mnName) {
		'''
		RETURN «mnName»'''
	}
	
}