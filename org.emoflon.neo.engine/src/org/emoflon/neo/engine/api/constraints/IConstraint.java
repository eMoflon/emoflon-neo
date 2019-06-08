package org.emoflon.neo.engine.api.constraints;

public interface IConstraint {
	boolean isSatisfied();

	default boolean isViolated() {
		return !isSatisfied();
	}
}
