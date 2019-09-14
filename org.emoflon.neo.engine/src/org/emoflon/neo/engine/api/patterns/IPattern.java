package org.emoflon.neo.engine.api.patterns;

import java.util.Collection;
import java.util.Optional;

public interface IPattern<M extends IMatch> {
	/**
	 * @return The name of the pattern.
	 */
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
	Collection<M> determineMatches();

	/**
	 * Only compute as most as many matches as required.
	 * 
	 * @return at most limit random matches for the pattern.
	 */
	Collection<M> determineMatches(int limit);

	/**
	 * Compute a single match for the pattern.
	 * 
	 * @return A single match or empty if there are no matches for the pattern.
	 */
	default Optional<M> determineOneMatch() {
		return determineMatches(1).stream().findAny();
	}

	/**
	 * Compute all matches but only return the number of matches found. Note: as
	 * this invokes {@link #determineMatches()}, pattern matching is performed anew.
	 * 
	 * @return
	 */
	default int countMatches() {
		return determineMatches().size();
	}
}
