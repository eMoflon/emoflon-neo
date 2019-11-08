package org.emoflon.neo.engine.modules.terminationcondition;

import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.ITerminationCondition;

public class NoMoreMatchesTerminationCondition implements ITerminationCondition<NeoMatch, NeoCoMatch> {
	private int totalRuleApplicationsInLastStep = 0;

	@Override
	public boolean isReached(MatchContainer<NeoMatch, NeoCoMatch> matchContainer) {
		var appliedRules = matchContainer.getNumberOfRuleApplications() - totalRuleApplicationsInLastStep;
		totalRuleApplicationsInLastStep = matchContainer.getNumberOfRuleApplications();
		return appliedRules == 0;
	}
}
