package org.emoflon.neo.engine.api.rules;

public interface IMatch {
	IPattern getPattern();

	boolean isStillValid();
}
