package org.emoflon.neo.engine.api.rules;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.engine.api.patterns.IPattern;

public interface IRule<M extends IMatch, CM extends ICoMatch> extends IPattern<M> {
	/**
	 * Apply the rule for the match m with default rule application semantics as SPO
	 * (this might fail if the rule is not applicable).
	 * 
	 * @param match The match at which to apply the rule.
	 * @return The comatch of the resulting rule application. Empty if the rule was
	 *         not applicable.
	 */
	Optional<CM> apply(M match);

	Optional<Collection<CM>> applyAll(Collection<M> matches);

	/**
	 * Apply the rule for a random match.
	 * 
	 * @param ras The rule applications semantics to use.
	 * @return The comatch if application was possible. Empty if not.
	 */
	default Optional<CM> apply() {
		return determineOneMatch().flatMap(m -> apply(m));
	}

	void useSPOSemantics(boolean spoSemantics);

	@Override
	default Stream<String> getPatternElts() {
		return getContextElts();
	}

	Stream<String> getCreatedElts();

	Stream<String> getContextElts();

	boolean hasApplicationConditions();

	boolean isStillApplicable(M m);

	public Map<String, Boolean> isStillApplicable(Collection<M> matches);
}
