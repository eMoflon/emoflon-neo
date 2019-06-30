package org.emoflon.neo.engine.api.rules;

import java.util.Optional;

public interface IRule extends IPattern {
	ICoMatch apply(IMatch match);

	/**
	 * Apply the rule for a random match.
	 * 
	 * @return The comatch if application was possible.
	 */
	default Optional<ICoMatch> apply() {
		return determineOneMatch().map(this::apply);
	}
}
