package org.emoflon.neo.engine.modules.updatepolicies;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IUpdatePolicy;

/**
 * Selects a single match. The selection is non-deterministic but not explicitly
 * random.
 */
public class AnySingleMatchUpdatePolicy implements IUpdatePolicy<NeoMatch, NeoCoMatch> {
	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Collection<NeoMatch>> selectMatches(
			MatchContainer<NeoMatch, NeoCoMatch> matches, IMonitor<NeoMatch, NeoCoMatch> progressMonitor) {
		Map<IRule<NeoMatch, NeoCoMatch>, Collection<NeoMatch>> selection = new HashMap<>();
		matches.stream()//
				.filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())//
				.findAny()//
				.ifPresent(entry -> entry.getValue().stream()//
						.findAny()//
						.ifPresent(m -> selection.put(entry.getKey(), List.of(m))));
		return selection;
	}
}
