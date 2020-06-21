package org.emoflon.neo.engine.generator;

import java.util.Collection;
import java.util.Map;

import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.engine.api.rules.ICoMatch;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.modules.ICleanupModule;
import org.emoflon.neo.engine.generator.modules.IMatchReprocessor;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IRuleScheduler;
import org.emoflon.neo.engine.generator.modules.IStartupModule;
import org.emoflon.neo.engine.generator.modules.ITerminationCondition;
import org.emoflon.neo.engine.generator.modules.IUpdatePolicy;

public abstract class Generator<M extends IMatch, C extends ICoMatch> {

	private IStartupModule startupModule;
	private ITerminationCondition<M, C> terminationCondition;
	private IRuleScheduler<M, C> ruleScheduler;
	private IUpdatePolicy<M, C> updatePolicy;
	private IMatchReprocessor<M, C> matchReprocessor;
	private ICleanupModule cleanupModule;
	private IMonitor<M, C> progressMonitor;
	private MatchContainer<M, C> matchContainer;

	public Generator(//
			Collection<? extends IRule<M, C>> allRules, //
			IStartupModule startupModule, //
			ITerminationCondition<M, C> terminationCondition, //
			IRuleScheduler<M, C> ruleScheduler, //
			IUpdatePolicy<M, C> updatePolicy, //
			IMatchReprocessor<M, C> matchReprocessor, //
			ICleanupModule cleanupModule, //
			IMonitor<M, C> progressMonitor) {
		this.startupModule = startupModule;
		this.terminationCondition = terminationCondition;
		this.ruleScheduler = ruleScheduler;
		this.updatePolicy = updatePolicy;
		this.matchReprocessor = matchReprocessor;
		this.cleanupModule = cleanupModule;
		this.progressMonitor = progressMonitor;
		matchContainer = createMatchContainer(allRules);
	}

	public void generate() {
		progressMonitor.startStartup(startupModule.description());
		startupModule.startup();
		progressMonitor.finishStartup();

		do {
			// Heartbeat for continuous feedback
			progressMonitor.heartBeat(matchContainer);
			
			// 1. Schedule rules for pattern matching
			progressMonitor.startRuleScheduling();
			var scheduledRules = ruleScheduler.scheduleWith(matchContainer, progressMonitor);
			progressMonitor.finishRuleScheduling();

			// 2. Perform pattern matching
			progressMonitor.startPatternMatching();
			determineMatches(scheduledRules, matchContainer);
			progressMonitor.finishPatternMatching();

			// 3. Match selection
			if (!matchContainer.getRulesWithMatches().isEmpty()) {
				progressMonitor.startMatchSelection();
				var selectedMatches = updatePolicy.selectMatches(matchContainer, progressMonitor);
				progressMonitor.finishMatchSelection();

				// 4. Rule application
				progressMonitor.startRuleApplication();
				selectedMatches.forEach((rule, matches) -> {
					applyMatches(rule, matches, matchContainer);
				});
				progressMonitor.finishRuleApplication();
			}

			// 5. Match reprocessing
			progressMonitor.startReprocessingMatches();
			matchReprocessor.reprocess(matchContainer, progressMonitor);
			progressMonitor.finishReprocessingMatches();
		} while (!terminationCondition.isReached(matchContainer));

		progressMonitor.startCleanup(cleanupModule.description());
		cleanupModule.cleanup();
		progressMonitor.finishCleanup();

		progressMonitor.finishGeneration(matchContainer);
	}

	protected abstract void applyMatches(IRule<M, C> rule, Collection<M> matches, MatchContainer<M, C> matchContainer);

	protected abstract void determineMatches(Map<IRule<M, C>, Schedule> scheduledRules, MatchContainer<M, C> matchContainer);

	protected abstract MatchContainer<M, C> createMatchContainer(Collection<? extends IRule<M, C>> allRules);
}
