package org.emoflon.neo.engine.modules.updatepolicies;

import java.util.Collection;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.cypher.rules.NeoRule;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.engine.generator.modules.ICleanupModule;
import org.emoflon.neo.engine.generator.modules.IInconsistencyReporter;
import org.emoflon.neo.engine.modules.ilp.ILPBasedOperationalStrategy;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;

public class BackwardTransformationOperationalStrategy extends ILPBasedOperationalStrategy implements ICleanupModule {

	public BackwardTransformationOperationalStrategy(//
			SupportedILPSolver solver, //
			NeoCoreBuilder builder, //
			Collection<NeoRule> genRules, //
			Collection<NeoRule> opRules, //
			Collection<IConstraint> negativeConstraints, //
			String sourceModel, //
			String targetModel//
	) {
		super(solver, genRules, opRules, negativeConstraints, builder, sourceModel, targetModel);
	}
	
	public BackwardTransformationOperationalStrategy(//
			SupportedILPSolver solver, //
			NeoCoreBuilder builder, //
			Collection<NeoRule> genRules, //
			Collection<NeoRule> opRules, //
			Collection<IConstraint> negativeConstraints, //
			IInconsistencyReporter inconsistencyReporter, //
			String sourceModel, //
			String targetModel//
	) {
		super(solver, genRules, opRules, negativeConstraints, inconsistencyReporter, builder, sourceModel, targetModel);
	}

	@Override
	public String description() {
		return "ILP solving, deletion of inconsistent corrs and source model elements";
	}
	
	@Override
	protected void removeInconsistentElements(Collection<Long> inconsistentElts) {
		removeInconsistentElements(inconsistentElts, true, true, false);
	}
}
