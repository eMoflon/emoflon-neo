package org.emoflon.neo.generator.modules;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.generator.IUpdatePolicy;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

/**
 * Selects a single match. The selection is non-deterministic but not explicitly
 * random.
 */
public class SimpleNeoUpdatePolicy implements IUpdatePolicy<NeoMatch, NeoCoMatch> {
	@Override
	public Collection<NeoMatch> selectMatches(Map<NeoMatch, IRule<NeoMatch, NeoCoMatch>> pMatches) {
		Collection<NeoMatch> matches = new HashSet<>();
		Optional<NeoMatch> selectedMatch = pMatches.keySet().stream().findAny();
		if (selectedMatch.isPresent())
			matches.add(selectedMatch.get());
		return matches;
	}
}
