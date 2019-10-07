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
			// 1. Schedule rules for pattern matching
			progressMonitor.startRuleScheduling();
			var scheduledRules = ruleScheduler.scheduleWith(matchContainer.getRulesWithoutMatches(), progressMonitor);
			progressMonitor.finishRuleScheduling();
			
			// 2. Perform pattern matching
			progressMonitor.startPatternMatching();
			scheduledRules.forEach((rule, count) -> matchContainer.addAll(rule.determineMatches(count), rule));
			progressMonitor.finishPatternMatching();
			
			// 3. Match selection
			progressMonitor.startMatchSelection();
			var selectedMatches = updatePolicy.selectMatches(matchContainer, progressMonitor);
			progressMonitor.finishMatchSelection();
			
			// 4. Rule application
			progressMonitor.startRuleApplication();
			selectedMatches.forEach((rule, matches) -> rule.applyAll(matches));
			progressMonitor.finishRuleApplication();
			
			// 5. Match reprocessing
			progressMonitor.startReprocessingMatches();
			matchReprocessor.reprocess(matchContainer, progressMonitor);
			progressMonitor.finishReprocessingMatches();
			
			// Heartbeat for continuous feedback
			progressMonitor.heartBeat();
		}
		
		progressMonitor.finishGeneration();
	}
}
