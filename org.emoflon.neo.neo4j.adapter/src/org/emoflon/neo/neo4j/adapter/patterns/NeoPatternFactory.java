package org.emoflon.neo.neo4j.adapter.patterns;

import org.emoflon.neo.emsl.eMSL.ConstraintReference;
import org.emoflon.neo.emsl.eMSL.NegativeConstraint;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.eMSL.PositiveConstraint;
import org.emoflon.neo.neo4j.adapter.models.EmptyBuilder;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.models.NeoCoreBuilder;

public class NeoPatternFactory {

	public static NeoPattern createNeoPattern(Pattern pattern) {
		return createNeoPattern(pattern, new EmptyBuilder(), new EmptyMask());
	}

	public static NeoPattern createNeoPattern(Pattern pattern, NeoCoreBuilder builder) {
		return createNeoPattern(pattern, builder, new EmptyMask());
	}

	public static NeoPattern createNeoPattern(Pattern pattern, IBuilder builder, NeoMask mask) {
		if (pattern.getCondition() == null)
			return new NeoPatternQueryAndMatchNoCondition(pattern, builder, mask);
		else if (pattern.getCondition() instanceof ConstraintReference)
			return new NeoPatternQueryAndMatchConstraintRef(pattern, builder, mask);
		else if (pattern.getCondition() instanceof PositiveConstraint)
			return new NeoPatternQueryAndMatchPositiveConstraint(pattern, builder, mask);
		else if (pattern.getCondition() instanceof NegativeConstraint)
			return new NeoPatternQueryAndMatchNegativeConstraint(pattern, builder, mask);
		else
			throw new IllegalArgumentException("Unknown type of pattern:" + pattern);
	}
}