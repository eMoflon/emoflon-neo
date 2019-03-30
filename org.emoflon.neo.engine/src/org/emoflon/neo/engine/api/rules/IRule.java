package org.emoflon.neo.engine.api.rules;

public interface IRule extends IPattern {
	ICoMatch apply(IMatch match);
}
