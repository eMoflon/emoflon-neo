package org.emoflon.neo.victory.adapter.matches

import java.util.Collection

class MatchQuery {
	static def String determineNeighbourhood(Collection<Long> nodes, int neighbourhoodSize) {
		'''
			«matchPath(nodes, neighbourhoodSize)»
			«checkIds(nodes, "n")»
			«returnStatement(nodes, "p")»
		'''
	}

	static def String getMatchEdges(Collection<Long> edges) {
		'''
			MATCH 
				«FOR e : edges SEPARATOR ', '»
					()-[r«e»]->()
				«ENDFOR»
				
			«checkIds(edges, "r")»
			«returnStatement(edges, "r")»
		'''
	}

	def static String matchPath(Collection<Long> nodes, int neighbourhoodSize) {
		'''
			MATCH 
				«FOR n : nodes SEPARATOR ', '»
					p«n»=(n«n»)-[*0..«neighbourhoodSize»]-()
				«ENDFOR»
		'''
	}

	def static String checkIds(Collection<Long> ids, String prefix) {
		'''
			WHERE 
				«FOR id : ids SEPARATOR ' AND '»
					id(«prefix»«id») = «id»
				«ENDFOR» 
		'''
	}

	def static String returnStatement(Collection<Long> ids, String prefix) {
		'''
			RETURN 
				«FOR id : ids SEPARATOR ',\n '»
					«prefix»«id»
				«ENDFOR»
		'''
	}

}
