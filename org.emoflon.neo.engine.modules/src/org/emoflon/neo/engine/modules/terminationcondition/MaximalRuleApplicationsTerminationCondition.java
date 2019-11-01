package org.emoflon.neo.engine.modules.terminationcondition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.ITerminationCondition;
import org.emoflon.neo.engine.modules.analysis.RuleAnalyser;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoRule;

public class MaximalRuleApplicationsTerminationCondition implements ITerminationCondition<NeoMatch, NeoCoMatch> {
	private Map<NeoRule, Integer> maxRuleApps;
	private Map<String, NeoRule> nameToRule;

	public MaximalRuleApplicationsTerminationCondition(Collection<NeoRule> allRules, int defaultMaxApps) {
		maxRuleApps = new HashMap<>();
		nameToRule = new HashMap<>();
		allRules.forEach(r -> {
			nameToRule.put(r.getName(), r);
			maxRuleApps.put(r, defaultMaxApps);
		});
	}

	@Override
	public boolean isReached(MatchContainer<NeoMatch, NeoCoMatch> matchContainer) {
		var maxedOutRules = new ArrayList<IRule<NeoMatch, NeoCoMatch>>();
		for (var rule : matchContainer.getAllRulesToMatches().keySet()) {
			var noOfApps = matchContainer.getNoOfRuleApplicationsFor(rule);
			if (getMaxNoOfAppFor(rule) >= 0 && noOfApps >= getMaxNoOfAppFor(rule))
				maxedOutRules.add(rule);
		}

		maxedOutRules.forEach(matchContainer::removeRule);

		return matchContainer.getAllRulesToMatches().isEmpty();
	}

	public int getMaxNoOfAppFor(IRule<NeoMatch, NeoCoMatch> rule) {
		return maxRuleApps.get(rule);
	}

	public MaximalRuleApplicationsTerminationCondition setMax(String rule, int value) {
		if (!nameToRule.containsKey(rule)) {
			throw new IllegalArgumentException(rule + " is not a valid name of a TGG rule in: " + nameToRule.keySet());
		}

		var r = nameToRule.get(rule);
		maxRuleApps.put(r, value);
		
		return this;
	}

	public void requireMaxForFreeAxioms() {
		var freeAxiomsWithoutMax = maxRuleApps.entrySet().stream()//
				.filter(entry -> RuleAnalyser.isFreeAxiom(entry.getKey().getEMSLRule()))//
				.filter(entry -> entry.getValue() < 0)//
				.map(entry -> entry.getKey().getName())//
				.collect(Collectors.toList());

		if (!freeAxiomsWithoutMax.isEmpty()) {
			throw new IllegalArgumentException(
					"You have to assign max values to the following free axioms: " + freeAxiomsWithoutMax);
		}
	}
}
