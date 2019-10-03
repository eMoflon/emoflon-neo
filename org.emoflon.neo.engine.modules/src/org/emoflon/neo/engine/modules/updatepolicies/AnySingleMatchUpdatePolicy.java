package org.emoflon.neo.engine.modules.updatepolicies;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class AnySingleMatchUpdatePolicy implements IUpdatePolicy<NeoMatch, NeoCoMatch> {
	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Collection<NeoMatch>> selectMatches(
			MatchContainer<NeoMatch, NeoCoMatch> matches, IMonitor progressMonitor) {
		Map<IRule<NeoMatch, NeoCoMatch>, Collection<NeoMatch>> selection = new HashMap<>();
		matches.stream()//
				.findAny()//
				.ifPresent(entry -> entry.getValue().stream()//
						.findAny()//
						.ifPresent(m -> selection.put(entry.getKey(), List.of(m))));
		return selection;
	}
}
