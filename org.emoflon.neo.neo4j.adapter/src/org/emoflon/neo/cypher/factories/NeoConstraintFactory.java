package org.emoflon.neo.cypher.factories;

import org.emoflon.neo.cypher.constraints.NeoConstraint;
import org.emoflon.neo.cypher.constraints.NeoNegativeConstraint;
import org.emoflon.neo.cypher.models.IBuilder;
import org.emoflon.neo.emsl.eMSL.Constraint;
import org.emoflon.neo.emsl.eMSL.NegativeConstraint;

public class NeoConstraintFactory {

	public static NeoConstraint createNeoConstraint(Constraint constraint) {
		return createNeoConstraint(constraint, IBuilder.empty());
	}

	public static NeoConstraint createNeoConstraint(Constraint constraint, IBuilder builder) {
		var body = constraint.getBody();
		if (body instanceof NegativeConstraint)
			return new NeoNegativeConstraint(constraint, builder);
		else
			return new NeoConstraint(constraint, builder);
	}
}
