package org.emoflon.neo.engine.api.patterns;

public interface IMatch {
	IPattern<?> getPattern();

	boolean isStillValid();
}
