package org.emoflon.neo.engine.api.rules;

import java.util.Optional;

public interface IRule<M extends IMatch, CM extends ICoMatch> extends IPattern<M> {

	/**
	 * Apply the rule for the match m (this might fail if the rule is not
	 * applicable).
	 * 
	 * @param match The match at which to apply the rule.
	 * @param ras   The rule application semantics to use.
	 * @return The comatch of the resulting rule application. Empty if the rule was
	 *         not applicable.
	 */
	Optional<CM> apply(M match, RuleApplicationSemantics ras);

	/**
	 * Apply the rule for the match m with default rule application semantics as DPO
	 * (this might fail if the rule is not applicable).
	 * 
	 * @param match The match at which to apply the rule.
	 * @return The comatch of the resulting rule application. Empty if the rule was
	 *         not applicable.
	 */
	default Optional<CM> apply(M match) {
		return apply(match, RuleApplicationSemantics.DoublePushOut);
	}

	/**
	 * Apply the rule for a random match.
	 * 
	 * @param ras The rule applications semantics to use.
	 * @return The comatch if application was possible. Empty if not.
	 */
	default Optional<CM> apply(RuleApplicationSemantics ras) {
		return determineOneMatch().flatMap(m -> apply(m, ras));
	}

	/**
	 * Apply the rule for a random match using DPO as default rule application
	 * semantics.
	 * 
	 * @return The comatch if application was possible. Empty if not.
	 */
	default Optional<CM> apply() {
		return apply(RuleApplicationSemantics.DoublePushOut);
	}
}
