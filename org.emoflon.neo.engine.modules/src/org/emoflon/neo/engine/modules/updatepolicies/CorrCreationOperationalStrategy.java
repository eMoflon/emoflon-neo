package org.emoflon.neo.engine.modules.updatepolicies;

import static org.emoflon.neo.engine.modules.analysis.RuleAnalyser.noCorrContext;
import static org.emoflon.neo.engine.modules.analysis.RuleAnalyser.toRule;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.emoflon.neo.cypher.models.IBuilder;
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
	private IBuilder builder;

	public CorrCreationOperationalStrategy(IBuilder builder, Collection<NeoRule> genRules, Collection<NeoRule> opRules,
			Collection<IConstraint> negativeConstraints) {
		super(genRules, opRules, negativeConstraints);
		this.builder = builder;
	}

	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Collection<NeoMatch>> selectMatches(
			MatchContainer<NeoMatch, NeoCoMatch> matchContainer, IMonitor<NeoMatch, NeoCoMatch> progressMonitor) {
		if (this.matchContainer == null)
			this.matchContainer = matchContainer;

		if (matchContainer.getNumberOfRuleApplications() > 0)
			matchContainer.getAllRulesToMatches().keySet().forEach(rule -> {
				if (noCorrContext(toRule(genRules.get(rule.getName())))) {
					matchContainer.removeRule(rule);
				}
			});

		return matchContainer.getAllRulesToMatches();
	}

	@Override
	public boolean isConsistent(SupportedILPSolver suppSolver) throws Exception {
		logger.debug("Registering all matches...");
		registerMatches(matchContainer.streamAllCoMatches());
		computeWeights();
		logger.debug("Registered all matches.");

		var intermediateResult = determineInconsistentElements(suppSolver);
		result = deleteInconsistentCorrs(intermediateResult);

		return result.isEmpty();
	}

	private Set<Long> deleteInconsistentCorrs(Set<Long> inconsistentElts) {
		var remaining = new HashSet<>(inconsistentElts);
		var corrs = inconsistentElts.stream()//
				.map(Math::abs)//
				.filter(this::isCreatedCorr)//
				.collect(Collectors.toSet());
		builder.deleteAll(corrs);
		remaining.removeAll(corrs.stream().map(x -> -1*x).collect(Collectors.toSet()));
		return remaining;
	}

	private boolean isCreatedCorr(Long element) {
		return matchContainer.getRelRange().getIDs().contains(element);
	}
}
