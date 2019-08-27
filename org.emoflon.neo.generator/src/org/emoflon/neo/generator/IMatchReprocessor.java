package org.emoflon.neo.generator;

import java.util.Map;

import org.emoflon.neo.engine.api.rules.ICoMatch;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.api.rules.IRule;

public interface IMatchReprocessor<M extends IMatch, C extends ICoMatch> {
	public void reprocess(Map<M, IRule<M, C>> pRemainingMatches);
}
