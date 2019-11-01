package org.emoflon.neo.neo4j.adapter.constraints;

import org.emoflon.neo.cypher.models.EmptyBuilder;
import org.emoflon.neo.cypher.models.IBuilder;
import org.emoflon.neo.emsl.eMSL.AndBody;
import org.emoflon.neo.emsl.eMSL.Constraint;
import org.emoflon.neo.emsl.eMSL.ConstraintReference;
import org.emoflon.neo.emsl.eMSL.Implication;
import org.emoflon.neo.emsl.eMSL.NegativeConstraint;
import org.emoflon.neo.emsl.eMSL.OrBody;
import org.emoflon.neo.emsl.eMSL.PositiveConstraint;
import org.emoflon.neo.neo4j.adapter.patterns.EmptyMask;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMask;
import org.emoflon.neo.neo4j.adapter.util.NeoQueryData;

public class NeoConstraintFactory {
	public static NeoConstraint createNeoConstraint(Constraint constraint) {
		return createNeoConstraint(constraint, new EmptyBuilder(), new NeoQueryData(false), new EmptyMask());
	}

	public static NeoConstraint createNeoConstraint(Constraint constraint, IBuilder builder) {
		return createNeoConstraint(constraint, builder, new NeoQueryData(false), new EmptyMask());
	}

	public static NeoConstraint createNeoConstraint(Constraint constraint, IBuilder builder, NeoQueryData queryData,
			NeoMask mask) {
		return createNeoConstraint(constraint, builder, queryData, mask, true);
	}

	public static NeoConstraint createNeoConstraint(Constraint constraint, IBuilder builder, NeoQueryData queryData,
			NeoMask mask, boolean injective) {
		return createNeoConstraint(constraint.getBody(), builder, queryData, mask, injective);
	}

	public static NeoConstraint createNeoConstraint(Object b, IBuilder builder, NeoQueryData queryData, NeoMask mask,
			boolean injective) {
		if (b instanceof PositiveConstraint) {
			var ap = ((PositiveConstraint) b).getPattern();
			return new NeoPositiveConstraint(ap, injective, builder, queryData, mask);
		} else if (b instanceof NegativeConstraint) {
			var ap = ((NegativeConstraint) b).getPattern();
			return new NeoNegativeConstraint(ap, injective, builder, queryData, mask);
		} else if (b instanceof OrBody) {
			return new NeoOrBody((OrBody) b, builder, queryData, mask, injective);
		} else if (b instanceof Implication) {
			var implication = (Implication) b;
			var apIf = implication.getPremise();
			var apThen = implication.getConclusion();
			return new NeoImplication(apIf, apThen, injective, builder, queryData, mask);
		} else if (b instanceof ConstraintReference) {
			return new NeoConstraintRef((ConstraintReference) b, builder, queryData, mask, injective);
		} else if (b instanceof AndBody)
			return new NeoAndBody((AndBody) b, builder, queryData, mask, injective);

		throw new IllegalArgumentException("Unknown type of constraint:" + b);
	}
}
