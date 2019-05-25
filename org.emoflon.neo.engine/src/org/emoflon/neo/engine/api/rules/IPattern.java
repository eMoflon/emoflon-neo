package org.emoflon.neo.engine.api.rules;

import java.util.Collection;

public interface IPattern {
	String getName();

	Collection<IMatch> getMatches();

	Collection<IMatch> getValidMatches(String uuid);

	default Number countMatches() {
		return getMatches().size();
	}
}
