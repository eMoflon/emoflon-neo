package org.emoflon.neo.victory.adapter.matches

class MatchQuery {

	static def String getMatchNodes(String nodeIds) {
		'''
			MATCH 
				(n)
			WHERE
				id(n) in $«nodeIds»
			RETURN
				n
		'''
	}
	
	static def String getNeighbouringNodes(String nodeIds, int neighbourhood){
		'''
			MATCH 
				(n)-[*1..«neighbourhood»]-(m)
			WHERE
				id(n) in $«nodeIds» AND NOT id(m) in $«nodeIds» 
			RETURN
				id(m)
		'''
	}

	static def String getMatchEdges(String edgeIds) {
		'''
			MATCH 
				()-[r]->()
			WHERE
				id(r) in $«edgeIds»
			RETURN
				r
		'''
	}
	
	static def String getAllEdges(String nodeIds){
		'''
			MATCH
				(from)-[r]->(to)
			WHERE
				id(from) in $«nodeIds» AND id(to) in $«nodeIds»
			RETURN
				id(r)
		'''
	}
}
