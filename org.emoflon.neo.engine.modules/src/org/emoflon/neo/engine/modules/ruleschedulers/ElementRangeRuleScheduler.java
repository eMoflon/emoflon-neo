package org.emoflon.neo.engine.modules.ruleschedulers;

import java.util.Map;
import java.util.stream.Collectors;

import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.NodeSampler;
import org.emoflon.neo.engine.generator.Schedule;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IRuleScheduler;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

import com.google.common.base.Functions;

public class ElementRangeRuleScheduler implements IRuleScheduler<NeoMatch, NeoCoMatch> {

	private NodeSampler sampler;

	public ElementRangeRuleScheduler(NodeSampler sampler) {
		this.sampler = sampler;
	}

	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Schedule> scheduleWith(//
			MatchContainer<NeoMatch, NeoCoMatch> matchContainer, //
			IMonitor<NeoMatch, NeoCoMatch> progressMonitor//
	) {
		var scheduledRules = matchContainer.streamAllRules()//
				.collect(Collectors.toMap(Functions.identity(), //
						r -> new Schedule(-1, matchContainer.getNodeRange(), r, sampler)));

		return scheduledRules;
	}
}