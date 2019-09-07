package org.emoflon.neo.engine.generator;

import org.emoflon.neo.engine.api.rules.ICoMatch;
import org.emoflon.neo.engine.api.rules.IMatch;

public class Generator<M extends IMatch, C extends ICoMatch> {

	private ITerminationCondition terminationCondition;
	private IRuleScheduler<M, C> ruleScheduler;
	private IUpdatePolicy<M, C> updatePolicy;
	private IMatchReprocessor<M, C> matchReprocessor;

	public Generator(ITerminationCondition pTerminationCondition, IRuleScheduler<M, C> pRuleScheduler,
			IUpdatePolicy<M, C> pUpdatePolicy, IMatchReprocessor<M, C> pMatchReprocessor) {
		terminationCondition = pTerminationCondition;
		ruleScheduler = pRuleScheduler;
		updatePolicy = pUpdatePolicy;
		matchReprocessor = pMatchReprocessor;
	}

	public void generate() {

		MatchContainer<M, C> matches = new MatchContainer<>();
		IMonitor monitor = null; // TODO

		while (!terminationCondition.isReached()) {

			ruleScheduler.scheduleWith(null, monitor).forEach(
					(rule, count) -> rule.determineMatches(count).forEach((match) -> matches.add(match, rule)));

			updatePolicy.selectMatches(matches, monitor).forEach((match) -> matches.remove(match).apply(match));

			matchReprocessor.reprocess(matches, monitor);
		}
	}
}
