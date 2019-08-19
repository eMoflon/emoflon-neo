package org.emoflon.neo.neo4j.adapter.constraints;

import org.emoflon.neo.emsl.eMSL.Constraint;
import org.emoflon.neo.emsl.eMSL.Implication;
import org.emoflon.neo.emsl.eMSL.NegativeConstraint;
import org.emoflon.neo.emsl.eMSL.OrBody;
import org.emoflon.neo.emsl.eMSL.PositiveConstraint;
import org.emoflon.neo.neo4j.adapter.models.EmptyBuilder;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.patterns.EmptyMask;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMask;
import org.emoflon.neo.neo4j.adapter.util.NeoHelper;

public class NeoConstraintFactory {
	public static NeoConstraint createNeoConstraint(Constraint constraint) {
		return createNeoConstraint(constraint, new EmptyBuilder(), new NeoHelper(), new EmptyMask());
	}
	
	public static NeoConstraint createNeoConstraint(Constraint constraint, IBuilder builder) {
		return createNeoConstraint(constraint, builder, new NeoHelper(), new EmptyMask());
	}
	
	public static NeoConstraint createNeoConstraint(Constraint constraint, IBuilder builder, NeoHelper helper, NeoMask mask) {
		return createNeoConstraint(constraint, builder, helper, mask, true);
	}
	
	public static NeoConstraint createNeoConstraint(Constraint constraint, IBuilder builder, NeoHelper helper, NeoMask mask, boolean injective) {
		if(constraint.getBody() instanceof PositiveConstraint) {
			var ap = ((PositiveConstraint)constraint.getBody()).getPattern();
			return new NeoPositiveConstraint(ap, injective, builder, helper, mask);
		} else if(constraint.getBody() instanceof NegativeConstraint) {
			var ap = ((NegativeConstraint)constraint.getBody()).getPattern();
			return new NeoNegativeConstraint(ap, injective, builder, helper, mask);
		} else if(constraint.getBody() instanceof OrBody) {
			return new NeoOrBody((OrBody) constraint.getBody(), builder, helper, mask, injective);
		} else if(constraint.getBody() instanceof Implication) {
			var implication = (Implication) constraint.getBody();
			var apIf = implication.getPremise();
			var apThen = implication.getConclusion();
			return new NeoImplication(apIf, apThen, injective, builder, helper, mask);
		}
		
		throw new IllegalArgumentException("Unknown type of constraint:" + constraint);
	}
}
