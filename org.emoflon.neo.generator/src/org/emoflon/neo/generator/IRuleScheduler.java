package org.emoflon.neo.generator;

import java.util.Map;

import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.engine.api.rules.ICoMatch;
import org.emoflon.neo.engine.api.rules.IRule;

public interface IRuleScheduler<M extends IMatch, C extends ICoMatch> {
	public Map<IRule<M, C>, Integer> scheduleWith(Map<M, IRule<M, C>> pAvailableMatches);
}