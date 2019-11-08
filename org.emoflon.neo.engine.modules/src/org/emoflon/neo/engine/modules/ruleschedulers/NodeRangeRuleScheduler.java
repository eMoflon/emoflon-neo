package org.emoflon.neo.engine.modules.ruleschedulers;

import java.util.Map;
import java.util.stream.Collectors;

import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.ElementRange;
import org.emoflon.neo.engine.generator.INodeSampler;
import org.emoflon.neo.engine.generator.IRelSampler;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.Schedule;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IRuleScheduler;

import com.google.common.base.Functions;

public class NodeRangeRuleScheduler implements IRuleScheduler<NeoMatch, NeoCoMatch> {

	private INodeSampler sampler;

	public NodeRangeRuleScheduler(INodeSampler sampler) {
		this.sampler = sampler;
	}

	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Schedule> scheduleWith(//
			MatchContainer<NeoMatch, NeoCoMatch> matchContainer, //
			IMonitor<NeoMatch, NeoCoMatch> progressMonitor//
	) {
		var scheduledRules = matchContainer.streamAllRules()//
				.collect(Collectors.toMap(Functions.identity(), //
						r -> new Schedule(-1, matchContainer.getNodeRange(), new ElementRange(), r, sampler, (type, ruleName) -> IRelSampler.EMPTY)));

		return scheduledRules;
	}
}
