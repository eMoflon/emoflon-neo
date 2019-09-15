package org.emoflon.neo.engine.modules.updatepolicies;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IUpdatePolicy;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

/**
 * Selects a single match. The selection is non-deterministic but not explicitly
 * random.
 */
public class SimpleNeoUpdatePolicy implements IUpdatePolicy<NeoMatch, NeoCoMatch> {
	@Override
	public Collection<NeoMatch> selectMatches(MatchContainer<NeoMatch, NeoCoMatch> pMatches,
			IMonitor pProgressMonitor) {
		Collection<NeoMatch> matches = new HashSet<>();
		Optional<NeoMatch> selectedMatch = pMatches.stream().findAny();
		if (selectedMatch.isPresent())
			matches.add(selectedMatch.get());
		return matches;
	}
}
