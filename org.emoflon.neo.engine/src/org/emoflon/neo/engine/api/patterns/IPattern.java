package org.emoflon.neo.engine.api.patterns;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.emoflon.neo.engine.generator.Schedule;

public interface IPattern<M extends IMatch> {
	/**
	 * @return The name of the pattern.
	 */
	String getName();

	/**
	 * Compute and return all matches. Note: every time this method is invoked
	 * pattern matching is performed anew.
	 * 
	 * @return Collection of all determined matches.
	 */
	default Collection<M> determineMatches() {
		return determineMatches(Schedule.unlimited());
	}

	/**
	 * Only compute as most as many matches as required.
	 * 
	 * @return at most limit random matches for the pattern.
	 */
	Collection<M> determineMatches(Schedule schedule, IMask mask);

	default Collection<M> determineMatches(Schedule schedule) {
		return determineMatches(schedule, IMask.empty());
	}

	default Optional<M> determineOneMatch() {
		return determineOneMatch(IMask.empty());
	}
	
	default Optional<M> determineOneMatch(IMask mask) {
		return determineMatches(Schedule.once(), mask).stream().findAny();
	}

	/**
	 * Compute all matches but only return the number of matches found. Note: as
	 * this invokes {@link #determineMatches()}, pattern matching is performed anew.
	 * 
	 * @return
	 */
	default int countMatches(IMask mask) {
		return determineMatches(Schedule.unlimited(), mask).size();
	}

	default int countMatches() {
		return countMatches(IMask.empty());
	}

	/**
	 * Check if a collection of matches for this pattern are still valid.
	 * 
	 * @param matches
	 * @return A map of match ids to bools signalling if the particular match is
	 *         still valid or not.
	 */
	Map<String, Boolean> isStillValid(Collection<M> matches);

	Collection<String> getContextNodeLabels();
	Collection<String> getContextRelLabels();
}
