package org.emoflon.neo.engine.generator.modules;

import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.engine.api.rules.ICoMatch;
import org.emoflon.neo.engine.generator.MatchContainer;

public interface IMatchReprocessor<M extends IMatch, C extends ICoMatch> {
	public void reprocess(MatchContainer<M, C> matchContainer, IMonitor progressMonitor);
}
