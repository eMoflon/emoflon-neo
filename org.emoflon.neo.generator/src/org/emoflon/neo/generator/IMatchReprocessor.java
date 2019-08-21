package org.emoflon.neo.generator;

import java.util.Collection;

import org.emoflon.neo.engine.api.rules.IMatch;

public interface IMatchReprocessor {
	public Collection<IMatch> reprocess(Collection<IMatch> pRemainingMatches);
}
