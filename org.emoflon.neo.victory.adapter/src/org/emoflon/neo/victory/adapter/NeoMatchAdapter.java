package org.emoflon.neo.victory.adapter;

import java.util.Collection;
import java.util.HashMap;

import org.emoflon.ibex.tgg.ui.debug.api.Graph;
import org.emoflon.ibex.tgg.ui.debug.api.Match;
import org.emoflon.ibex.tgg.ui.debug.api.Rule;
import org.emoflon.ibex.tgg.ui.debug.api.impl.GraphBuilder;
import org.emoflon.neo.engine.api.patterns.IMatch;

public class NeoMatchAdapter implements Match {
	private IMatch match;
	private Collection<NeoRuleAdapter> rules;

	public NeoMatchAdapter(IMatch match, Collection<NeoRuleAdapter> rules) {
		this.match = match;
		this.rules = rules;
	}

	public IMatch getWrappedMatch() {
		return match;
	}

	@Override
	public String getName() {
		return match.getPattern().getName();
	}

	@Override
	public boolean isBlocked() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getBlockingReason() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public Rule getRule() {
		return rules.stream().filter(r -> r.getName().equals(getName())).findAny().get();
	}

	@Override
	public Graph getGraph(int pNeighbourhoodSize) {
		var rule = getRule();
		var builder = new GraphBuilder();
		var nameToNode = new HashMap<String, NeoNodeAdapter>();
		
		//1.  Do I have to build the entire graph consisting of both rule and match?
		//2.  Domain and action for match nodes is rather weird...  Isn't this clear from the corresponding rule nodes?
				
		return builder.build();
	}
}
