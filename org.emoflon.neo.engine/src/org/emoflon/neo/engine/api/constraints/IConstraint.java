package org.emoflon.neo.engine.api.constraints;

public interface IConstraint {
	boolean holds();

	boolean isViolated();
}
