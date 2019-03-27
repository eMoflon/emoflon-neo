package org.emoflon.neo.engine.api.transformation;

import java.util.Collection;

import org.emoflon.neo.engine.api.rules.IMatch;

public interface ITransformationPolicy {
	IMatch chooseOneMatch(Collection<IMatch> matches);

	boolean continueTransformation();

	boolean greedyChoiceAllowed();
}
