package org.emoflon.neo.neo4j.adapter;

import org.emoflon.neo.emsl.eMSL.Constraint;
import org.emoflon.neo.engine.api.constraints.IConstraint;

/**
 * TODO[Jannik] Implement constraints
 * 
 */
public class NeoConstraintPositive extends NeoConstraint {

	public NeoConstraintPositive(Constraint c, NeoCoreBuilder builder) {
		super(c, builder);
	}
	
	

	@Override
	public boolean isSatisfied() {
		// TODO
		return false;
	}

}
