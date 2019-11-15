package org.emoflon.neo.engine.modules.updatepolicies;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.log4j.Logger;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.cypher.rules.NeoRule;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.ICleanupModule;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.modules.ilp.ILPBasedOperationalStrategy;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;

public class CheckOnlyOperationalStrategy extends ILPBasedOperationalStrategy implements ICleanupModule {
	private static final Logger logger = Logger.getLogger(CheckOnlyOperationalStrategy.class);

	public CheckOnlyOperationalStrategy(SupportedILPSolver solver, Collection<NeoRule> genRules,
			Collection<NeoRule> opRules, Collection<IConstraint> negativeConstraints, NeoCoreBuilder builder,
			String sourceModel, String targetModel) {
		super(solver, genRules, opRules, negativeConstraints, builder, sourceModel, targetModel);
	}

	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Collection<NeoMatch>> selectMatches(
			MatchContainer<NeoMatch, NeoCoMatch> matches, IMonitor<NeoMatch, NeoCoMatch> progressMonitor) {
		logger.debug("Registering all matches...");

		// Precedence information
		registerMatches(matches.streamAllMatches());
		computeWeights();

		logger.debug("Registered all matches.");

		return Collections.emptyMap();
	}

	@Override
	public void cleanup() {
		try {
			if (isConsistent()) {
				logger.info("Your triple is consistent!");
			} else {
				logger.info("Your triple is inconsistent!");
				var inconsistentElements = determineInconsistentElements();
				logger.info(inconsistentElements.size() + " elements of your triple are inconsistent!");
				logger.debug(inconsistentElements);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String description() {
		return "Clean Up:  Solve ILP problem to determine consistency of provided triple.";
	}
}
