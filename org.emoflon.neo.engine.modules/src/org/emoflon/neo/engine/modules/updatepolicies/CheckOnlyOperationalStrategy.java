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
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.modules.ilp.ILPBasedOperationalStrategy;

public class CheckOnlyOperationalStrategy extends ILPBasedOperationalStrategy {
	private static final Logger logger = Logger.getLogger(CheckOnlyOperationalStrategy.class);

	public CheckOnlyOperationalStrategy(Collection<NeoRule> genRules, Collection<NeoRule> opRules,
			Collection<IConstraint> negativeConstraints, NeoCoreBuilder builder, String sourceModel,
			String targetModel) {
		super(genRules, opRules, negativeConstraints, builder, sourceModel, targetModel);
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
}
