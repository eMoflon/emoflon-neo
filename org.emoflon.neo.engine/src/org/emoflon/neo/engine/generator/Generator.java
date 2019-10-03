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
			ITerminationCondition terminationCondition, //
			IRuleScheduler<M, C> ruleScheduler, //
			IUpdatePolicy<M, C> updatePolicy, //
			IMatchReprocessor<M, C> matchReprocessor, //
			IMonitor progressMonitor) {
		this.terminationCondition = terminationCondition;
		this.ruleScheduler = ruleScheduler;
		this.updatePolicy = updatePolicy;
		this.matchReprocessor = matchReprocessor;
		this.progressMonitor = progressMonitor;
	}

	public void generate(Collection<IRule<M, C>> allRules) {
		MatchContainer<M, C> matchContainer = new MatchContainer<>(allRules);
		while (!terminationCondition.isReached()) {
			ruleScheduler.scheduleWith(matchContainer.getRulesWithoutMatches(), progressMonitor)//
					.forEach((rule, count) -> matchContainer.addAll(rule.determineMatches(count), rule));

			updatePolicy.selectMatches(matchContainer, progressMonitor)//
					.forEach((rule, matches) -> rule.applyAll(matches));

			matchReprocessor.reprocess(matchContainer, progressMonitor);
		}
	}
}
