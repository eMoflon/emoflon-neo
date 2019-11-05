package org.emoflon.neo.engine.modules.ruleschedulers;

import java.util.HashMap;
import java.util.Map;

import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.Schedule;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IRuleScheduler;

public class FixedNoOfMatchesRuleScheduler implements IRuleScheduler<NeoMatch, NeoCoMatch> {

	private int noOfMatches;

	public FixedNoOfMatchesRuleScheduler(int noOfMatches) {
		this.noOfMatches = noOfMatches;
	}

	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Schedule> scheduleWith(MatchContainer<NeoMatch, NeoCoMatch> matchContainer,
			IMonitor<NeoMatch, NeoCoMatch> progressMonitor) {
		Map<IRule<NeoMatch, NeoCoMatch>, Schedule> scheduleMap = new HashMap<>();
		matchContainer.getRulesWithoutMatches().forEach(rule -> scheduleMap.put(rule, new Schedule(noOfMatches)));
		return scheduleMap;
	}
}
