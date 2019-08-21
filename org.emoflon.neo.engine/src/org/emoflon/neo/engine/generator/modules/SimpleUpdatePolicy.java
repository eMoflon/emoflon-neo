package org.emoflon.neo.engine.generator.modules;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.generator.IUpdatePolicy;

/**
 * Selects a single match. The selection is non-deterministic but not explicitly
 * random.
 */
public class SimpleUpdatePolicy implements IUpdatePolicy {
	@Override
	public Collection<IMatch> selectMatches(Collection<IMatch> pMatches) {
		Collection<IMatch> matches = new HashSet<>();
		Optional<IMatch> selectedMatch = pMatches.stream().findAny();
		if (selectedMatch.isPresent())
			matches.add(selectedMatch.get());
		return matches;
	}
}
