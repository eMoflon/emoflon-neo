package org.emoflon.neo.engine.modules.ruleschedulers;

import java.util.Map;

import org.apache.log4j.Logger;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.INodeSampler;
import org.emoflon.neo.engine.generator.Schedule;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IRuleScheduler;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

public class TwoPhaseRuleSchedulerForGEN implements IRuleScheduler<NeoMatch, NeoCoMatch> {

	private static final Logger logger = Logger.getLogger(TwoPhaseRuleSchedulerForGEN.class);
	private FreeAxiomsRuleSchedulerForGEN phase1 = new FreeAxiomsRuleSchedulerForGEN();
	private INodeSampler sampler;
	private ElementRangeRuleScheduler phase2 = null;
	

	public TwoPhaseRuleSchedulerForGEN(INodeSampler sampler) {
		this.sampler = sampler;
	}

	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Schedule> scheduleWith(//
			MatchContainer<NeoMatch, NeoCoMatch> matchContainer, //
			IMonitor<NeoMatch, NeoCoMatch> progressMonitor//
	) {
		if (phase2 == null) {
			logger.info("Applying all free axioms...");
			var scheduledRules = phase1.scheduleWith(matchContainer, progressMonitor);
			phase2 = new ElementRangeRuleScheduler(sampler);
			return scheduledRules;
		}

		return phase2.scheduleWith(matchContainer, progressMonitor);
	}
}