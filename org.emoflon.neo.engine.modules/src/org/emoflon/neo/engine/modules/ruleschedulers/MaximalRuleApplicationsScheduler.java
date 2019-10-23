package org.emoflon.neo.engine.modules.ruleschedulers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IRuleScheduler;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoRule;

public class MaximalRuleApplicationsScheduler implements IRuleScheduler<NeoMatch, NeoCoMatch> {
	private Map<IRule<NeoMatch, NeoCoMatch>, Integer> maxRuleApps;
	private Map<String, IRule<NeoMatch, NeoCoMatch>> nameToRule;

	public MaximalRuleApplicationsScheduler(Collection<NeoRule> allRules, int defaultNoOfApps) {
		maxRuleApps = new HashMap<>();
		nameToRule = new HashMap<>();
		allRules.forEach(r -> {
			nameToRule.put(r.getName(), r);
			maxRuleApps.put(r, defaultNoOfApps);
		});
	}

	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Integer> scheduleWith(MatchContainer<NeoMatch, NeoCoMatch> matchContainer,
			IMonitor<NeoMatch, NeoCoMatch> progressMonitor) {
		var allRules = new HashMap<IRule<NeoMatch, NeoCoMatch>, Integer>();

		for (var rule : matchContainer.getAllRulesToMatches().keySet()) {
			var noOfApps = matchContainer.getNoOfRuleApplicationsFor(rule);
			if (getMaxNoOfAppFor(rule) < 0)
				allRules.put(rule, -1);
			else if (noOfApps < getMaxNoOfAppFor(rule))
				allRules.put(rule, getRemainingApps(rule, noOfApps));
		}

		return allRules;
	}

	private int getRemainingApps(IRule<NeoMatch, NeoCoMatch> rule, int noOfApps) {
		return maxRuleApps.get(rule) - noOfApps;
	}

	private int getMaxNoOfAppFor(IRule<NeoMatch, NeoCoMatch> rule) {
		return maxRuleApps.get(rule);
	}

	public void setMaxNoOfApplicationsFor(String rule, int value) {
		if (!nameToRule.containsKey(rule)) {
			throw new IllegalArgumentException(rule + " is not a valid name of a TGG rule in: " + nameToRule.keySet());
		}

		var r = nameToRule.get(rule);
		maxRuleApps.put(r, value);
	}

}
