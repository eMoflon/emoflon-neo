package org.emoflon.neo.victory.adapter.matches

import org.emoflon.neo.engine.api.patterns.IMatch
import org.emoflon.victory.ui.api.Rule

class MatchQuery {
	static def String create(IMatch match, Rule rule, int neighbourhoodSize) {
		'''
			«matchPath(match,neighbourhoodSize)»
			«checkIds(match,neighbourhoodSize)»
			«returnPath(match,neighbourhoodSize)»
		'''
	}

	static def String getMatchEdges(long edgeId) {
		'''
			MATCH ()-[r]->() WHERE id(r)= «edgeId» RETURN r
		'''
	}

	def static String matchPath(IMatch match, int neighbourhoodSize) {
		'''
			MATCH «FOR n : match.elements SEPARATOR ', '»
						p«n»=(n«n»)- [*0..«neighbourhoodSize»]-(m«n»)
			«ENDFOR»
		'''
	}

	def static String checkIds(IMatch match, int neighbourhoodSize) {
		'''
			WHERE «FOR n : match.elements SEPARATOR ' AND '»
				id(n«n») = «n»«ENDFOR» 
		'''
	}

	def static String returnPath(IMatch match, int neighbourhoodSize) {
		'''
			RETURN «FOR n : match.elements SEPARATOR ',\n '»
								p«n»
			«ENDFOR»
		'''
	}

}
