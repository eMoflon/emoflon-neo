package org.emoflon.neo.engine.modules.ruleschedulers;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IRuleScheduler;
import org.emoflon.neo.engine.modules.analysis.RuleAnalyser;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

import com.google.common.base.Functions;

public class TwoPhaseRuleSchedulerForGEN implements IRuleScheduler<NeoMatch, NeoCoMatch> {

	private static final Logger logger = Logger.getLogger(TwoPhaseRuleSchedulerForGEN.class);
	private Phase1RuleSchedulerForGEN phase1 = new Phase1RuleSchedulerForGEN();
	private Phase2RuleSchedulerForGEN phase2 = null;

	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Integer> scheduleWith(//
			MatchContainer<NeoMatch, NeoCoMatch> matchContainer, //
			IMonitor<NeoMatch, NeoCoMatch> progressMonitor//
	) {
		if (phase1 != null) {
			// Try Phase I
			var freeAxioms = matchContainer.getAllRulesToMatches().keySet().stream()//
					.filter(r -> RuleAnalyser.isFreeAxiom(RuleAnalyser.toRule(r)))//
					.collect(Collectors.toList());

			Map<IRule<NeoMatch, NeoCoMatch>, Integer> scheduledRules = Collections.emptyMap();
			if (!freeAxioms.isEmpty()) {
				logger.info("Executing Phase I of GEN with: " + freeAxioms);
				scheduledRules = phase1.scheduleWith(matchContainer, progressMonitor);
			} else {
				logger.info("Skipping Phase I of GEN as there are no free axioms!");
			}

			phase1 = null;

			if (!scheduledRules.isEmpty())
				return scheduledRules;
		}

		if (phase2 == null) {
			logger.info("Executing Phase II of GEN: First round of pattern matching can take some time ...");
			phase2 = new Phase2RuleSchedulerForGEN();
		}

		var scheduledRules = phase2.scheduleWith(matchContainer, progressMonitor);
		logger.debug("Scheduled: " + scheduledRules + " for pattern matching.");
		
		return scheduledRules;
	}
}

class Phase1RuleSchedulerForGEN implements IRuleScheduler<NeoMatch, NeoCoMatch> {

	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Integer> scheduleWith(//
			MatchContainer<NeoMatch, NeoCoMatch> matchContainer, //
			IMonitor<NeoMatch, NeoCoMatch> progressMonitor//
	) {
		return matchContainer.getAllRulesToMatches().keySet().stream()//
				.filter(r -> RuleAnalyser.isFreeAxiom(RuleAnalyser.toRule(r)))//
				.collect(Collectors.toMap(x -> x, x -> 1));
	}

}

class Phase2RuleSchedulerForGEN implements IRuleScheduler<NeoMatch, NeoCoMatch> {

	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Integer> scheduleWith(//
			MatchContainer<NeoMatch, NeoCoMatch> matchContainer, //
			IMonitor<NeoMatch, NeoCoMatch> progressMonitor//
	) {
		// TODO[Anjorin]: Determine activated rules
		// TODO[Anjorin]: Only ask for "new" matches of activated rules
		// TODO[Anjorin]: Only ask when enough new elements have been created perhaps
		// (configurable)?
//		return matchContainer.getAllRulesToMatches().entrySet().stream()
//				.collect(Collectors.toMap(Entry::getKey, entry -> -1));
		//TODO[Anjorin] Number of matches should be configurable?
		return matchContainer.getRulesWithoutMatches().stream()
				.collect(Collectors.toMap(Functions.identity(), Functions.constant(-1)));
	}
}
