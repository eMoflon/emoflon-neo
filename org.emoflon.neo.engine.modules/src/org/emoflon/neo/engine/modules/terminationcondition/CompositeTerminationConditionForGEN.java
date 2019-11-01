package org.emoflon.neo.engine.modules.terminationcondition;

import java.util.concurrent.TimeUnit;

import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.ITerminationCondition;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

public class CompositeTerminationConditionForGEN implements ITerminationCondition<NeoMatch, NeoCoMatch> {

	private ITerminationCondition<NeoMatch, NeoCoMatch> timeout;
	private ITerminationCondition<NeoMatch, NeoCoMatch> maxGeneratedElements;
	private MaximalRuleApplicationsTerminationCondition maxRuleApps;
	private int ruleApplicationsInLastStep = 0;
	private int noRulesApplied = 0;

	protected CompositeTerminationConditionForGEN(//
			ITerminationCondition<NeoMatch, NeoCoMatch> timeout, //
			ITerminationCondition<NeoMatch, NeoCoMatch> maxGeneratedElements, //
			MaximalRuleApplicationsTerminationCondition maxRuleApps//
	) {
		this.timeout = timeout;
		this.maxGeneratedElements = maxGeneratedElements;
		this.maxRuleApps = maxRuleApps;

		this.maxRuleApps.requireMaxForFreeAxioms();
	}

	public CompositeTerminationConditionForGEN(//
			NeoCoreBuilder builder, //
			long maxNoOfElements, //
			MaximalRuleApplicationsTerminationCondition maxRuleApps//
	) {
		this(//
				new NoTerminationCondition(), //
				new MaxGeneratedElementsTerminationCondition(maxNoOfElements, builder), //
				maxRuleApps//
		);
	}

	public CompositeTerminationConditionForGEN(//
			long maxDuration, //
			TimeUnit timeUnit, //
			MaximalRuleApplicationsTerminationCondition maxRuleApps//
	) {
		this(//
				new TimedTerminationCondition(maxDuration, timeUnit), //
				new NoTerminationCondition(), //
				maxRuleApps);
	}

	public CompositeTerminationConditionForGEN(//
			NeoCoreBuilder builder, //
			long maxDuration, //
			TimeUnit timeUnit, //
			long maxNoOfElements, //
			MaximalRuleApplicationsTerminationCondition maxRuleApps//
	) {

		this(//
				new TimedTerminationCondition(maxDuration, timeUnit), //
				new MaxGeneratedElementsTerminationCondition(maxNoOfElements, builder), //
				maxRuleApps);
	}

	@Override
	public boolean isReached(MatchContainer<NeoMatch, NeoCoMatch> matchContainer) {
		var appliedRules = matchContainer.getNumberOfRuleApplications() - ruleApplicationsInLastStep;
		ruleApplicationsInLastStep = matchContainer.getNumberOfRuleApplications();
		noRulesApplied = appliedRules == 0 ? noRulesApplied + 1 : 0;

		return matchContainer.hasNoRules()//
				|| timeout.isReached(matchContainer)//
				|| maxGeneratedElements.isReached(matchContainer)//
				|| maxRuleApps.isReached(matchContainer) || noRulesApplied > 500;
	}

}
