package org.emoflon.neo.engine.api.rules;

import java.util.Optional;

public interface IRule<M extends IMatch, CM extends ICoMatch> extends IPattern<M> {
	CM apply(M match);

	/**
	 * Apply the rule for a random match.
	 * 
	 * @return The comatch if application was possible.
	 */
	default Optional<CM> apply() {
		return determineOneMatch().map(this::apply);
	}
}
