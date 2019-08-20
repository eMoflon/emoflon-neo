package org.emoflon.neo.neo4j.adapter.patterns;

import java.util.List;

import org.emoflon.neo.emsl.eMSL.Condition;
import org.emoflon.neo.emsl.eMSL.ConstraintReference;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.NegativeConstraint;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.eMSL.PositiveConstraint;
import org.emoflon.neo.neo4j.adapter.models.EmptyBuilder;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.models.NeoCoreBuilder;
import org.emoflon.neo.neo4j.adapter.util.NeoQueryData;
import org.emoflon.neo.neo4j.adapter.util.NeoUtil;

public class NeoPatternFactory {

	public static NeoPattern createNeoPattern(Pattern pattern) {
		return createNeoPattern(pattern, new EmptyBuilder(), new EmptyMask());
	}

	public static NeoPattern createNeoPattern(Pattern pattern, NeoCoreBuilder builder) {
		return createNeoPattern(pattern, builder, new EmptyMask());
	}

	public static NeoPattern createNeoPattern(Pattern pattern, IBuilder builder, NeoMask mask) {
		var flatPattern = NeoUtil.getFlattenedPattern(pattern);
		var nodeBlocks = flatPattern.getBody().getNodeBlocks();
		var name = flatPattern.getBody().getName();

		return createNeoPattern(name, nodeBlocks, flatPattern.getCondition(), builder, mask);
	}

	public static NeoPattern createNeoPattern(String name, List<ModelNodeBlock> nodeBlocks, Condition c, IBuilder builder, NeoMask mask) {
		if (c == null)
			return new NeoPatternQueryAndMatchNoCondition(nodeBlocks, name, builder, mask, new NeoQueryData());
		else if (c instanceof ConstraintReference) {
			ConstraintReference ref = (ConstraintReference) c;
			return new NeoPatternQueryAndMatchConstraintRef(nodeBlocks, name, ref, builder, mask, new NeoQueryData());
		} else if (c instanceof PositiveConstraint) {
			PositiveConstraint pconstr = (PositiveConstraint) c;
			return new NeoPatternQueryAndMatchPositiveConstraint(nodeBlocks, name, pconstr, builder, mask,
					new NeoQueryData());
		} else if (c instanceof NegativeConstraint) {
			NegativeConstraint nconstr = (NegativeConstraint) c;
			return new NeoPatternQueryAndMatchNegativeConstraint(nodeBlocks, name, nconstr, builder, mask,
					new NeoQueryData());
		} else
			throw new IllegalArgumentException("Unknown type of pattern:" + name);
	}
}