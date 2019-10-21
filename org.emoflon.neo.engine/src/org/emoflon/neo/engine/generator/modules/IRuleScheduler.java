package org.emoflon.neo.engine.generator.modules;

import java.util.Map;

import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.engine.api.rules.ICoMatch;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.MatchContainer;

public interface IRuleScheduler<M extends IMatch, C extends ICoMatch> {
	public Map<IRule<M, C>, Integer> scheduleWith(MatchContainer<M, C> matchContainer, IMonitor progressMonitor);
}