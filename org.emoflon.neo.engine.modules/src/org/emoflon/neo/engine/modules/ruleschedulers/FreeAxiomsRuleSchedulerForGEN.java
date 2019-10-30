package org.emoflon.neo.engine.modules.ruleschedulers;

import java.util.Map;
import java.util.stream.Collectors;

import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.Schedule;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IRuleScheduler;
import org.emoflon.neo.engine.modules.analysis.RuleAnalyser;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

public class FreeAxiomsRuleSchedulerForGEN implements IRuleScheduler<NeoMatch, NeoCoMatch> {
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