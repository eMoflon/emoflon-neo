package org.emoflon.neo.victory.adapter;

import org.emoflon.ibex.tgg.ui.debug.api.Graph;
import org.emoflon.ibex.tgg.ui.debug.api.Match;
import org.emoflon.ibex.tgg.ui.debug.api.Rule;
import org.emoflon.neo.engine.api.patterns.IMatch;

public class NeoMatchAdapter implements Match {
	private IMatch match;

	private NeoMatchAdapter(IMatch match) {
		this.match = match;
	}

	public IMatch getWrappedMatch() {
		return match;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isBlocked() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getBlockingReason() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Rule getRule() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Graph getGraph(int pNeighbourhoodSize) {
		// TODO Auto-generated method stub
		return null;
	}
}
