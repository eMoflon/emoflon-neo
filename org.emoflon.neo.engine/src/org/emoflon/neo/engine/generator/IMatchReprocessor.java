package org.emoflon.neo.engine.generator;

import org.emoflon.neo.engine.api.rules.ICoMatch;
import org.emoflon.neo.engine.api.rules.IMatch;

public interface IMatchReprocessor<M extends IMatch, C extends ICoMatch> {
	public void reprocess(MatchContainer<M, C> pRemainingMatches, IMonitor pProgressMonitor);
}
