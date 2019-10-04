package org.emoflon.neo.engine.modules.updatepolicies;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IUpdatePolicy;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

/**
 * Selects a single match. The selection is non-deterministic but not explicitly
 * random.
 */
public class RandomSingleMatchUpdatePolicy implements IUpdatePolicy<NeoMatch, NeoCoMatch> {
	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Collection<NeoMatch>> selectMatches(
			MatchContainer<NeoMatch, NeoCoMatch> matches, IMonitor progressMonitor) {
		Map<IRule<NeoMatch, NeoCoMatch>, Collection<NeoMatch>> selection = new HashMap<>();
		
		var lst = matches.stream().collect(Collectors.toList());
		var entry = lst.get((int) Math.random()*lst.size());
		selection.put(entry.getKey(), entry.getValue());
	
		return selection;
	}
}
