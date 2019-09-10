package org.emoflon.neo.engine.generator;

import java.util.Collection;
import java.util.Map;

import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.engine.api.rules.ICoMatch;
import org.emoflon.neo.engine.api.rules.IRule;

public interface IRuleScheduler<M extends IMatch, C extends ICoMatch> {
	public Map<IRule<M, C>, Integer> scheduleWith(Collection<IRule<M, C>> pAvailableRules, IMonitor pProgressMonitor);
}