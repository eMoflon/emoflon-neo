package org.emoflon.neo.engine.generator;

import java.util.Collection;

import org.emoflon.neo.engine.api.rules.IMatch;

public interface IUpdatePolicy {
	public Collection<IMatch> selectMatches(Collection<IMatch> pMatches);
}
