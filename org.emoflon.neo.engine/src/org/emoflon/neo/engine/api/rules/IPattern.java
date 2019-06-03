package org.emoflon.neo.engine.api.rules;

import java.util.Collection;

public interface IPattern {
	String getName();

	/**
	 * Used to configure if pattern matching should be injective or not.
	 * 
	 * @param injective
	 */
	void setMatchInjectively(Boolean injective);

	/**
	 * Compute and return all matches. Note: every time this method is invoked
	 * pattern matching is performed anew.
	 * 
	 * @return Collection of all determined matches.
	 */
	Collection<IMatch> determineMatches();

	/**
	 * Compute all matches but only return the number of matches found. Note: as
	 * this invokes {@link #determineMatches()}, pattern matching is performed anew.
	 * 
	 * @return
	 */
	default Number countMatches() {
		return determineMatches().size();
	}
}
