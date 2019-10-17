package org.emoflon.neo.victory.adapter

import org.emoflon.neo.engine.api.patterns.IMatch
import org.emoflon.victory.ui.api.Rule

class MatchQuery {
	static def String create(IMatch match, Rule rule, int neighbourhoodSize) {
		'''
			match p1=(n1)-[*0..«neighbourhoodSize»]-(m1)  where id(n1) = 66160 return p1
		'''
	}
}
