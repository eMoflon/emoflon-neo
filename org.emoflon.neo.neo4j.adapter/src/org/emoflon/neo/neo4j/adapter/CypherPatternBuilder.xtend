package org.emoflon.neo.neo4j.adapter

import java.util.ArrayList
import java.util.Collection

class CypherPatternBuilder {
	
	def static String createCypherQuery(Collection<NeoNode> nodes) {
		'''
		MATCH «FOR n:nodes SEPARATOR ', '»«n.toString»«IF !n.relations.isEmpty», «ENDIF»«FOR r:n.relations SEPARATOR ', '»«n.toStringWithoutClassType +r.toString»«ENDFOR»«ENDFOR»
		RETURN «FOR n:nodes SEPARATOR ', '»«n.varName»«ENDFOR»
		'''
	}
	
}