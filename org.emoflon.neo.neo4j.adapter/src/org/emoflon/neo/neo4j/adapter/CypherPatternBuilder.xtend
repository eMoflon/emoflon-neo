package org.emoflon.neo.neo4j.adapter

import java.util.Collection

class CypherPatternBuilder {
	
	def static String createCypherQuery(Collection<NeoNode> nodes, int countCond) {
		getMatchQuery(nodes) +
		getConditionQuery(nodes,countCond) + 
		getReturnQuery(nodes);
	}
	
	def static String getMatchQuery(Collection<NeoNode> nodes) {

		'''
		MATCH «FOR n:nodes SEPARATOR ', '»«n.toString» «getRelationsQuery(n)» «ENDFOR»
		''' 
	}
	
	def static getRelationsQuery(NeoNode node) {
		
		'''
		«IF !node.relations.isEmpty», «ENDIF»«FOR r:node.relations SEPARATOR ', '»«node.toStringWithoutClassType +r.toString»«ENDFOR»
		'''
	}
	
	def static String getReturnQuery(Collection<NeoNode> nodes) {
		'''
		RETURN «FOR n:nodes SEPARATOR ', '»«n.varName»«ENDFOR»
		'''
	}
	
	def static String getConditionQuery(Collection<NeoNode> nodes, int countCond) {
		'''
		«IF countCond > 0»
			WHERE  «FOR n:nodes SEPARATOR ' AND'»«IF !n.conditions.isEmpty»«ENDIF»«FOR r:n.conditions SEPARATOR ' AND'»«r.toString»«ENDFOR»«ENDFOR»
		«ENDIF»
		''' 
	}
	
	
}