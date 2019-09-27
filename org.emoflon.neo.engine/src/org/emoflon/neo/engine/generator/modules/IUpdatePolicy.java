package org.emoflon.neo.engine.generator.modules;

import java.util.Collection;
import java.util.Map;

import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.engine.api.rules.ICoMatch;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.MatchContainer;

public interface IUpdatePolicy<M extends IMatch, C extends ICoMatch> {
	public Map<IRule<M, C>, Collection<M>> selectMatches(MatchContainer<M, C> matches, IMonitor progressMonitor);
}
