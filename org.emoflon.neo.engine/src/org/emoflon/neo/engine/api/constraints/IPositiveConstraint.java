package org.emoflon.neo.engine.api.constraints;

import org.emoflon.neo.engine.api.patterns.IMatch;

public interface IPositiveConstraint extends IConstraint {
	IMatch getMatch();
}
