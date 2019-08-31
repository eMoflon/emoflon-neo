package org.emoflon.neo.generator;

import java.util.Collection;
import java.util.Map;

import org.emoflon.neo.engine.api.rules.ICoMatch;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.api.rules.IRule;

public interface IUpdatePolicy<M extends IMatch, C extends ICoMatch> {
	public Collection<M> selectMatches(Map<M, IRule<M, C>> pMatches);
}
