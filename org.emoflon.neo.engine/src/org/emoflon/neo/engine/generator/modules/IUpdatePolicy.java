package org.emoflon.neo.engine.generator.modules;

import java.util.Collection;

import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.engine.api.rules.ICoMatch;
import org.emoflon.neo.engine.generator.MatchContainer;

public interface IUpdatePolicy<M extends IMatch, C extends ICoMatch> {
	public Collection<M> selectMatches(MatchContainer<M, C> pMatches, IMonitor pProgressMonitor);
}
