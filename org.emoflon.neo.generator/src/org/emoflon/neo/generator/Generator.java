package org.emoflon.neo.generator;

import java.util.HashMap;
import java.util.Map;

import org.emoflon.neo.engine.api.rules.ICoMatch;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.api.rules.IRule;

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

		Map<M, IRule<M, C>> matches = new HashMap<>();

		while (!terminationCondition.isReached()) {

			ruleScheduler.scheduleWith(matches).forEach(
					(rule, count) -> rule.determineMatches(count).forEach((match) -> matches.put(match, rule)));

			updatePolicy.selectMatches(matches).forEach((match) -> {
				IRule<M, C> rule = matches.remove(match);
				rule.apply(match);
			});

			matchReprocessor.reprocess(matches);
		}
	}
}
