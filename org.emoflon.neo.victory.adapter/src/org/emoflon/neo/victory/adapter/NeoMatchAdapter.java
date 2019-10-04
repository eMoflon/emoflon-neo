package org.emoflon.neo.victory.adapter;

import java.util.Collection;
import java.util.HashMap;

import org.emoflon.ibex.tgg.ui.debug.api.Graph;
import org.emoflon.ibex.tgg.ui.debug.api.Match;
import org.emoflon.ibex.tgg.ui.debug.api.Rule;
import org.emoflon.ibex.tgg.ui.debug.api.impl.GraphBuilder;
import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.neo4j.adapter.models.NeoCoreBuilder;

public class NeoMatchAdapter implements Match {
	private IMatch match;
	private Collection<NeoRuleAdapter> rules;
	private NeoCoreBuilder builder;
	
	public NeoMatchAdapter(NeoCoreBuilder builder, IMatch match, Collection<NeoRuleAdapter> rules) {
		this.match = match;
		this.rules = rules;
		this.builder = builder;
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
		var graphBuilder = new GraphBuilder();
		var nameToNode = new HashMap<String, NeoNodeAdapter>();
		
		//2.  Domain and action for match nodes is rather weird...  Isn't this clear from the corresponding rule nodes?
		var result = builder.executeQuery("match p=(n)-[*1..2]-(m)  where id(n) = 66160 return relationships(p)");		
		System.out.println(result.list().size());
		
		return graphBuilder.build();
	}
}
