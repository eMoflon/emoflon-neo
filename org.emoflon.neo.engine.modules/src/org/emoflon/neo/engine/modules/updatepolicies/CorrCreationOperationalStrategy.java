package org.emoflon.neo.engine.modules.updatepolicies;

import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.cypher.rules.NeoRule;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.modules.ilp.ILPBasedOperationalStrategy;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;

public class CorrCreationOperationalStrategy extends ILPBasedOperationalStrategy {
	private static final Logger logger = Logger.getLogger(CheckOnlyOperationalStrategy.class);
	private MatchContainer<NeoMatch, NeoCoMatch> matchContainer;

	public CorrCreationOperationalStrategy(Collection<NeoRule> genRules, Collection<NeoRule> opRules,
			Collection<IConstraint> negativeConstraints) {
		super(genRules, opRules, negativeConstraints);
	}

	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Collection<NeoMatch>> selectMatches(
			MatchContainer<NeoMatch, NeoCoMatch> matchContainer, IMonitor<NeoMatch, NeoCoMatch> progressMonitor) {
		if (this.matchContainer == null)
			this.matchContainer = matchContainer;
		return matchContainer.getAllRulesToMatches();
	}

	@Override
	public boolean isConsistent(SupportedILPSolver suppSolver) throws Exception {
		logger.debug("Registering all matches...");

		// Precedence information
		registerMatches(matchContainer.streamAllCoMatches());
		computeWeights();

		logger.debug("Registered all matches.");

		return super.isConsistent(suppSolver);
	}
}
