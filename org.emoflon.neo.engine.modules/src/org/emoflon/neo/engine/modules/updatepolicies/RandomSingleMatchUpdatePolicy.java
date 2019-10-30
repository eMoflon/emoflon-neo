package org.emoflon.neo.engine.modules.updatepolicies;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IUpdatePolicy;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

/**
 * Selects a single match. The selection is non-deterministic but not explicitly
 * random.
 */
public class RandomSingleMatchUpdatePolicy implements IUpdatePolicy<NeoMatch, NeoCoMatch> {

	private static Logger logger = Logger.getLogger(RandomSingleMatchUpdatePolicy.class);

	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Collection<NeoMatch>> selectMatches(MatchContainer<NeoMatch, NeoCoMatch> matches,
			IMonitor<NeoMatch, NeoCoMatch> progressMonitor) {
		Map<IRule<NeoMatch, NeoCoMatch>, Collection<NeoMatch>> selection = new HashMap<>();

		var rules = matches.getRulesWithMatches();
		
		var randomRule = rules.get((int) (Math.random() * rules.size()));

		var matchesOfRule = matches.matchesForRule(randomRule).collect(Collectors.toList());
		var randomMatch = matchesOfRule.get((int) (Math.random() * matchesOfRule.size()));

		logger.info("Chose match@" + randomRule + " randomly from " + rules + " rules, " +  matchesOfRule.size() + "/" + matches.streamAllMatches().count() + " matches");

		selection.put(randomRule, List.of(randomMatch));

		return selection;
	}
}
