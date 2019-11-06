package org.emoflon.neo.engine.api.rules;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.emoflon.neo.engine.api.patterns.IMask;
import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.engine.api.patterns.IPattern;

public interface IRule<M extends IMatch, CM extends ICoMatch> extends IPattern<M> {

	Collection<CM> applyAll(Collection<M> matches, IMask mask);

	default Collection<CM> applyAll(Collection<M> matches) {
		return applyAll(matches, IMask.empty());
	}

	default Optional<CM> apply(M match, IMask mask) {
		return applyAll(List.of(match), mask).stream().findAny();
	}

	default Optional<CM> apply(M match) {
		return apply(match, IMask.empty());
	}

	default Optional<CM> apply(IMask preMask, IMask postMask) {
		return determineOneMatch(preMask).flatMap(m -> apply(m, postMask));
	}

	default Optional<CM> apply() {
		return apply(IMask.empty(), IMask.empty());
	}

	void setSPOSemantics(boolean spoSemantics);

	boolean getSPOSemantics();

	Collection<String> getCreatedElts();

	default Collection<String> getContextElts() {
		return getElements();
	}

	boolean hasApplicationConditions();

	default Map<String, Boolean> isStillApplicable(Collection<M> matches) {
		return isStillValid(matches);
	}
}
