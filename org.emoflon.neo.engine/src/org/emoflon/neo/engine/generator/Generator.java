package org.emoflon.neo.engine.generator;

import java.util.Collection;

import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.engine.api.rules.ICoMatch;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.modules.IMatchReprocessor;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IRuleScheduler;
import org.emoflon.neo.engine.generator.modules.ITerminationCondition;
import org.emoflon.neo.engine.generator.modules.IUpdatePolicy;

public class Generator<M extends IMatch, C extends ICoMatch> {

	private ITerminationCondition terminationCondition;
	private IRuleScheduler<M, C> ruleScheduler;
	private IUpdatePolicy<M, C> updatePolicy;
	private IMatchReprocessor<M, C> matchReprocessor;
	private IMonitor progressMonitor;

	public Generator(//
			ITerminationCondition pTerminationCondition, //
			IRuleScheduler<M, C> pRuleScheduler, //
			IUpdatePolicy<M, C> pUpdatePolicy, //
			IMatchReprocessor<M, C> pMatchReprocessor, //
			IMonitor pProgressMonitor) {
		terminationCondition = pTerminationCondition;
		ruleScheduler = pRuleScheduler;
		updatePolicy = pUpdatePolicy;
		matchReprocessor = pMatchReprocessor;
		progressMonitor = pProgressMonitor;
	}

	public void generate(Collection<IRule<M, C>> pAllRules) {
		MatchContainer<M, C> matchContainer = new MatchContainer<>(pAllRules);
		while (!terminationCondition.isReached()) {
			ruleScheduler.scheduleWith(matchContainer.getRulesWithoutMatches(), progressMonitor)//
					.forEach((rule, count) -> rule.determineMatches(count)//
							.forEach((match) -> matchContainer.add(match, rule)));

			updatePolicy.selectMatches(matchContainer, progressMonitor)//
					.forEach((match) -> matchContainer.getRuleFor(match).apply(match));

			matchReprocessor.reprocess(matchContainer, progressMonitor);
		}
	}
}
