package org.emoflon.neo.cypher.factories;

import org.emoflon.neo.cypher.constraints.NeoConstraint;
import org.emoflon.neo.cypher.models.IBuilder;
import org.emoflon.neo.emsl.eMSL.Constraint;

public class NeoConstraintFactory {

	public static NeoConstraint createNeoConstraint(Constraint constraint) {
		return createNeoConstraint(constraint, IBuilder.empty());
	}

	public static NeoConstraint createNeoConstraint(Constraint constraint, IBuilder builder) {
		return new NeoConstraint(constraint, builder);
	}
}
