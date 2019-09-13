package org.emoflon.neo.engine.generator;

import java.util.Collection;

import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.engine.api.rules.ICoMatch;

public interface IUpdatePolicy<M extends IMatch, C extends ICoMatch> {
	public Collection<M> selectMatches(MatchContainer<M, C> pMatches, IMonitor pProgressMonitor);
}
