package org.emoflon.neo.engine.modules.ruleschedulers;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.NodeRange;
import org.emoflon.neo.engine.generator.Schedule;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IRuleScheduler;
import org.emoflon.neo.engine.modules.analysis.RuleAnalyser;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

import com.google.common.base.Functions;

public class TwoPhaseRuleSchedulerForGEN implements IRuleScheduler<NeoMatch, NeoCoMatch> {

	private static final Logger logger = Logger.getLogger(TwoPhaseRuleSchedulerForGEN.class);
	private FreeAxiomsRuleSchedulerForGEN phase1 = new FreeAxiomsRuleSchedulerForGEN();
	private ElementRangeRuleScheduler phase2 = null;
	private int intervalSize;
	private Collection<String> restrictedTypes;

	public TwoPhaseRuleSchedulerForGEN(int intervalSize, Collection<String> restrictedTypes) {
		this.intervalSize = intervalSize;
		this.restrictedTypes = restrictedTypes;
	}

	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Schedule> scheduleWith(//
			MatchContainer<NeoMatch, NeoCoMatch> matchContainer, //
			IMonitor<NeoMatch, NeoCoMatch> progressMonitor//
	) {
		if (phase2 == null) {
			logger.info("Executing Phase I of GEN");
			var scheduledRules = phase1.scheduleWith(matchContainer, progressMonitor);
			phase2 = new ElementRangeRuleScheduler(intervalSize, restrictedTypes);
			return scheduledRules;
		}

		return phase2.scheduleWith(matchContainer, progressMonitor);
	}
}

class FreeAxiomsRuleSchedulerForGEN implements IRuleScheduler<NeoMatch, NeoCoMatch> {

	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Schedule> scheduleWith(//
			MatchContainer<NeoMatch, NeoCoMatch> matchContainer, //
			IMonitor<NeoMatch, NeoCoMatch> progressMonitor//
	) {
		return matchContainer.getAllRulesToMatches().keySet().stream()//
				.filter(r -> RuleAnalyser.isFreeAxiom(RuleAnalyser.toRule(r)))//
				.collect(Collectors.toMap(x -> x, x -> Schedule.once()));
	}

}

class ElementRangeRuleScheduler implements IRuleScheduler<NeoMatch, NeoCoMatch> {

	private NodeRange range;
	private int intervalSize;
	private Collection<String> restrictedTypes;

	public ElementRangeRuleScheduler(int intervalSize, Collection<String> restrictedTypes) {
		this.intervalSize = intervalSize;
		this.restrictedTypes = restrictedTypes;
	}

	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Schedule> scheduleWith(//
			MatchContainer<NeoMatch, NeoCoMatch> matchContainer, //
			IMonitor<NeoMatch, NeoCoMatch> progressMonitor//
	) {
		if (range == null)
			range = matchContainer.getNodeRange();

		var scheduledRules = matchContainer.streamAllRules()//
				.collect(Collectors.toMap(Functions.identity(), //
						r -> new Schedule(-1, range.randomSampling(intervalSize, restrictedTypes))));

		return scheduledRules;
	}
}
