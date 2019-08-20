package org.emoflon.neo.neo4j.adapter.rules;

import org.emoflon.neo.emsl.eMSL.ConstraintReference;
import org.emoflon.neo.emsl.eMSL.NegativeConstraint;
import org.emoflon.neo.emsl.eMSL.PositiveConstraint;
import org.emoflon.neo.emsl.eMSL.Rule;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.patterns.EmptyMask;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMask;
import org.emoflon.neo.neo4j.adapter.patterns.NeoPatternFactory;
import org.emoflon.neo.neo4j.adapter.util.NeoQueryData;
import org.emoflon.neo.neo4j.adapter.util.NeoUtil;

public class NeoRuleFactory {

	public static NeoRule createNeoRule(Rule r, IBuilder builder) {
		return createNeoRule(r, builder, new EmptyMask());
	}

	public static NeoRule createNeoRule(Rule r, IBuilder builder, NeoMask mask) {
		var flatRule = NeoUtil.getFlattenedRule(r);
		var nodeBlocks = flatRule.getNodeBlocks();
		var contextPattern = NeoPatternFactory.createNeoPattern(flatRule.getName(), nodeBlocks, flatRule.getCondition(),
				builder, mask);

		if (r.getCondition() == null)
			return new NeoRuleQueryAndMatchNoCondition(r, contextPattern, builder, mask, new NeoQueryData());
		else if (r.getCondition() instanceof ConstraintReference)
			return new NeoRuleQueryAndMatchConstraintRef(r, contextPattern, builder, mask, new NeoQueryData());
		else if (r.getCondition() instanceof PositiveConstraint)
			return new NeoRuleQueryAndMatchPositiveConstraint(r, contextPattern, builder, mask, new NeoQueryData());
		else if (r.getCondition() instanceof NegativeConstraint)
			return new NeoRuleQueryAndMatchNegativeConstraint(r, contextPattern, builder, mask, new NeoQueryData());
		else
			throw new IllegalArgumentException("Unknown type of r: " + r);
	}
}