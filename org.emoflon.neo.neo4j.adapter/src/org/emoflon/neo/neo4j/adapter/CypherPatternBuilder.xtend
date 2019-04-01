package org.emoflon.neo.neo4j.adapter

import java.util.ArrayList
import java.util.Collection

class CypherPatternBuilder {
	
	def static String createCypherQuery(Collection<NeoNode> nodes) {
		'''
		MATCH «FOR n:nodes BEFORE '(' SEPARATOR '), (' AFTER ')'»«n.toString»«ENDFOR»
		RETURN «FOR n:nodes SEPARATOR ', '»«n.varName»«ENDFOR»
		'''
	}
	
}