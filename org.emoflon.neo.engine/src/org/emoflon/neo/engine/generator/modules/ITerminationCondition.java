package org.emoflon.neo.engine.generator.modules;

import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.engine.api.rules.ICoMatch;
import org.emoflon.neo.engine.generator.MatchContainer;

public interface ITerminationCondition<M extends IMatch, C extends ICoMatch> {
	public boolean isReached(MatchContainer<M, C> matchContainer);
}
