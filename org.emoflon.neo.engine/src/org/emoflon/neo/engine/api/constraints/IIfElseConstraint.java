package org.emoflon.neo.engine.api.constraints;

import java.util.Collection;

import org.emoflon.neo.engine.api.patterns.IMatch;

public interface IIfElseConstraint extends IConstraint {
	Collection<IMatch> getViolations();
}
