package org.emoflon.neo.engine.api.constraints;

import org.emoflon.neo.engine.api.rules.IMatch;

public interface IPositiveConstraint extends IConstraint {
	IMatch getMatch();
}
