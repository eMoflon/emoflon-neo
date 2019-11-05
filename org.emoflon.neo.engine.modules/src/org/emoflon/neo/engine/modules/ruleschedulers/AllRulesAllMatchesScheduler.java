package org.emoflon.neo.engine.modules.ruleschedulers;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.Schedule;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IRuleScheduler;

public class AllRulesAllMatchesScheduler implements IRuleScheduler<NeoMatch, NeoCoMatch> {

	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Schedule> scheduleWith(//
			MatchContainer<NeoMatch, NeoCoMatch> matchContainer, //
			IMonitor<NeoMatch, NeoCoMatch> progressMonitor//
	) {
		return matchContainer.getAllRulesToMatches().entrySet().stream()//
				.collect(Collectors.toMap(Entry::getKey, entry -> Schedule.unlimited()));
	}

}
