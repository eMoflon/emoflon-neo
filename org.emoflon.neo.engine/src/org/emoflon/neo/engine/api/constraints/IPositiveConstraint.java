package org.emoflon.neo.engine.api.constraints;

import java.util.Collection;

import org.emoflon.neo.engine.api.patterns.IMatch;

public interface IPositiveConstraint<M extends IMatch> extends IConstraint {
	Collection<M> getPremise();
	Collection<M> getConclusion();
}
