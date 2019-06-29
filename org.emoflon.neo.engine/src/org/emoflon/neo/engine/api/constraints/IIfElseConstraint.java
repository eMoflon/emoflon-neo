package org.emoflon.neo.engine.api.constraints;

import java.util.Collection;

import org.emoflon.neo.engine.api.rules.IMatch;

public interface IIfElseConstraint {
	Collection<IMatch> getViolations();
}
