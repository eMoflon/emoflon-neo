package org.emoflon.neo.neo4j.adapter.patterns;

import java.util.List;

import org.emoflon.neo.emsl.eMSL.AtomicPattern;
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
	
	public static NeoPattern createNeoPattern(AtomicPattern pattern, IBuilder builder, NeoMask mask) {
		var flatPattern = NeoUtil.getFlattenedPattern(pattern);
		var nodeBlocks = flatPattern.getNodeBlocks();
		var name = flatPattern.getName();

		return createNeoPattern(name, nodeBlocks, null, builder, mask);
	}

	public static NeoPattern createNeoPattern(String name, List<ModelNodeBlock> nodeBlocks, Condition c, IBuilder builder, NeoMask mask) {
		return createNeoPattern(name, nodeBlocks, c, builder, mask, new NeoQueryData(true)); 
	}
	
	public static NeoPattern createNeoPattern(String name, List<ModelNodeBlock> nodeBlocks, Condition c, IBuilder builder, NeoMask mask, NeoQueryData query) {
		if (c == null)
			return new NeoPatternQueryAndMatchNoCondition(nodeBlocks, name, builder, mask, query);
		else if (c instanceof ConstraintReference) {
			ConstraintReference ref = (ConstraintReference) c;
			return new NeoPatternQueryAndMatchConstraintRef(nodeBlocks, name, ref, builder, mask, query);
		} else if (c instanceof PositiveConstraint) {
			PositiveConstraint pconstr = (PositiveConstraint) c;
			return new NeoPatternQueryAndMatchPositiveConstraint(nodeBlocks, name, pconstr, builder, mask,
					query);
		} else if (c instanceof NegativeConstraint) {
			NegativeConstraint nconstr = (NegativeConstraint) c;
			return new NeoPatternQueryAndMatchNegativeConstraint(nodeBlocks, name, nconstr, builder, mask,
					query);
		} else
			throw new IllegalArgumentException("Unknown type of pattern:" + name);
	}

	public static NeoPattern createNeoCoPattern(String name, List<ModelNodeBlock> nodeBlocks, Condition condition,
			IBuilder builder, NeoMask mask) {
		return createNeoPattern(name, nodeBlocks, condition, builder, mask, new NeoQueryData(false));
	}
}