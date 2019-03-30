package org.emoflon.neo.engine.api.rules;

public interface IMatch {
	IRule getRule();

	boolean isStillValid();
	
	void destroy();
}
