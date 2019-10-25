package org.emoflon.neo.engine.modules.updatepolicies;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IUpdatePolicy;
import org.emoflon.neo.engine.modules.analysis.RuleAnalyser;
import org.emoflon.neo.engine.modules.terminationcondition.MaximalRuleApplicationsTerminationCondition;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

public class TwoPhaseUpdatePolicyForGEN implements IUpdatePolicy<NeoMatch, NeoCoMatch> {

	private boolean phase1 = true;
	private RandomSingleMatchUpdatePolicy randomSingleChoice = new RandomSingleMatchUpdatePolicy();
	private MaximalRuleApplicationsTerminationCondition maxRuleApps;

	public TwoPhaseUpdatePolicyForGEN(MaximalRuleApplicationsTerminationCondition maxRuleApps) {
		this.maxRuleApps = maxRuleApps;
	}

	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Collection<NeoMatch>> selectMatches(//
			MatchContainer<NeoMatch, NeoCoMatch> matches, //
			IMonitor<NeoMatch, NeoCoMatch> progressMonitor//
	) {
		if (phase1) {
			var selection = new HashMap<IRule<NeoMatch, NeoCoMatch>, Collection<NeoMatch>>();
			var freeAxioms = matches.getRulesWithMatches();
			freeAxioms.forEach(fa -> selection.put(fa, maxCopies(fa, matches.getAllRulesToMatches().get(fa))));
			phase1 = false;
			return selection;

		}

		while (!matches.hasNoMatches()) {
			var randomSelection = randomSingleChoice.selectMatches(matches, progressMonitor);
			var chosenRule = randomSelection.keySet().iterator().next();
			var chosenMatch = randomSelection.get(chosenRule).iterator().next();

			if (matches.isPristine(chosenMatch)//
					|| RuleAnalyser.isFree(RuleAnalyser.toRule(chosenRule)) //
					|| chosenMatch.isStillValid())
				return randomSelection;
			else
				matches.removeMatch(chosenRule, chosenMatch);
		}

		return Collections.emptyMap();
	}

	private Collection<NeoMatch> maxCopies(IRule<NeoMatch, NeoCoMatch> rule, Collection<NeoMatch> singleMatch) {
		var match = singleMatch.iterator().next();
		return Stream.generate(() -> new NeoMatch(match))//
				.limit(maxRuleApps.getMaxNoOfAppFor(rule))//
				.collect(Collectors.toList());
	}
}
