package org.emoflon.debug.eneo.adapter.EneoAdapter;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import org.emoflon.ibex.tgg.ui.debug.api.Graph;
import org.emoflon.ibex.tgg.ui.debug.api.Match;
import org.emoflon.ibex.tgg.ui.debug.api.Rule;
import org.emoflon.ibex.tgg.ui.debug.api.enums.Domain;
import org.emoflon.ibex.tgg.ui.debug.api.impl.GraphBuilder;
import org.emoflon.neo.engine.api.patterns.IMatch;

public class EneoMatchAdapter implements Match {
    private static Map<IMatch, EneoMatchAdapter> wrappers = new HashMap<>();

    public static EneoMatchAdapter adapt(IMatch pMatch) {
	if (!wrappers.containsKey(pMatch))
	    wrappers.put(pMatch, new EneoMatchAdapter(pMatch));
	return wrappers.get(pMatch);
    }

    // ----------

    private IMatch match;
    private Map<Integer, Graph> graphs = new HashMap<>();

    private EneoMatchAdapter(IMatch pMatch) {
	match = pMatch;
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

    private void buildGraph(int pNeighbourhoodSize) {
	// TODO Build match graph
    }

}
