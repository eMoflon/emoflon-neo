package org.emoflon.neo.victory.adapter

import org.emoflon.neo.engine.api.patterns.IMatch
import org.emoflon.ibex.tgg.ui.debug.api.Rule

class MatchQuery {
	static def String create(IMatch match, Rule rule, int neighbourhoodSize) {
	'''
		«matchPath(match,neighbourhoodSize)»
		«checkIds(match,neighbourhoodSize)»
		«returnPath(match,neighbourhoodSize)»
	'''	
	

}
static def String getMatchEdges(long edgeId){
	'''
	MATCH ()-[r]->() WHERE id(r)= «edgeId» RETURN r
	'''
}
def static String matchPath(IMatch match, int neighbourhoodSize) {
	'''
		MATCH «FOR n : match.nodeIDs.values SEPARATOR ', '»
			p«n»=(n«n»)- [*0..«neighbourhoodSize»]-(m«n»)
			«ENDFOR»
	'''
	}
	
def static String checkIds(IMatch match, int neighbourhoodSize) {
	'''
		WHERE «FOR n : match.nodeIDs.values SEPARATOR ' AND '»
		 id(n«n») = «n»«ENDFOR» 
	'''
	}
def static String returnPath(IMatch match, int neighbourhoodSize) {
	'''
		RETURN «FOR n : match.nodeIDs.values SEPARATOR ',\n '»
					p«n»
		«ENDFOR»
	'''
	}

}
	
