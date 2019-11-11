package org.emoflon.neo.engine.modules.updatepolicies;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IUpdatePolicy;
import org.emoflon.neo.engine.modules.analysis.RuleAnalyser;
import org.emoflon.neo.engine.modules.terminationcondition.MaximalRuleApplicationsTerminationCondition;

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
			var freeAxioms = matches.getRulesWithMatches().stream()//
					.filter(r -> RuleAnalyser.isFreeAxiom(RuleAnalyser.toRule(r)));
			var selection = new HashMap<IRule<NeoMatch, NeoCoMatch>, Collection<NeoMatch>>();
			freeAxioms.forEach(fa -> selection.put(fa, maxCopies(fa, matches.getAllRulesToMatches().get(fa))));
			phase1 = false;
			if (!selection.isEmpty())
				return selection;
		}

		while (!matches.hasNoMatches()) {
			var randomSelection = randomSingleChoice.selectMatches(matches, progressMonitor);
			return randomSelection;
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
