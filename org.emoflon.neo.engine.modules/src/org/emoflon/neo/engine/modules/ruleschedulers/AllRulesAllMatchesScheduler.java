package org.emoflon.neo.engine.modules.ruleschedulers;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IRuleScheduler;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

public class AllRulesAllMatchesScheduler implements IRuleScheduler<NeoMatch, NeoCoMatch> {

	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Integer> scheduleWith(//
			MatchContainer<NeoMatch, NeoCoMatch> matchContainer, //
			IMonitor<NeoMatch, NeoCoMatch> progressMonitor//
	) {
		return matchContainer.getAllRulesToMatches().entrySet().stream()//
				.collect(Collectors.toMap(Entry::getKey, entry -> -1));
	}

}
