package org.emoflon.neo.victory.adapter;

import java.util.Collection;

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
	public Graph getGraph(int neighbourhoodSize) {
		var graphBuilder = new GraphBuilder();
		
		var result = builder.executeQuery(MatchQuery.create(match, getRule(), neighbourhoodSize));		
		System.out.println(result.list().size());
		System.out.println(result.keys());
		
		return graphBuilder.build();
	}
}
