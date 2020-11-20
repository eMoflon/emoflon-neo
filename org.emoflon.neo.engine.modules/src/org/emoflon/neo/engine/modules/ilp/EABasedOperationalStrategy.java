package org.emoflon.neo.engine.modules.ilp;

import java.util.Collection;

import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.cypher.rules.NeoRule;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;

public abstract class EABasedOperationalStrategy extends ILPBasedOperationalStrategy {

	public EABasedOperationalStrategy(SupportedILPSolver solver, Collection<NeoRule> genRules,
			Collection<NeoRule> opRules, Collection<IConstraint> negativeConstraints, NeoCoreBuilder builder,
			String sourceModel, String targetModel) {
		super(solver, genRules, opRules, negativeConstraints, builder, sourceModel, targetModel);
	}

}
