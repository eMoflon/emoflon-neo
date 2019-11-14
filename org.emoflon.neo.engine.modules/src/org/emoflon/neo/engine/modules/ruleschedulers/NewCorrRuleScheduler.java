package org.emoflon.neo.engine.modules.ruleschedulers;

import static org.emoflon.neo.engine.modules.analysis.RuleAnalyser.noCorrContext;
import static org.emoflon.neo.engine.modules.analysis.RuleAnalyser.toRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.emoflon.neo.cypher.models.NeoCoreBootstrapper;
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

/**
 * This scheduler exploits the fact that rules with corr context can never match
 * for CC if there were no corrs created in the last step (they must have been
 * collected already).
 * 
 * @author aanjorin
 */
public class NewCorrRuleScheduler implements IRuleScheduler<NeoMatch, NeoCoMatch> {

	private Collection<Object> allCorrIDsUpToLastStep = new ArrayList<>();

	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Schedule> scheduleWith(//
			MatchContainer<NeoMatch, NeoCoMatch> matchContainer, //
			IMonitor<NeoMatch, NeoCoMatch> progressMonitor//
	) {
		var latestCorrIDs = matchContainer.getRelRange().remove(allCorrIDsUpToLastStep);
		allCorrIDsUpToLastStep = matchContainer.getRelRange().getIDs();

		IRelSampler sampler = (type, ruleName) -> {
			return type.equals(NeoCoreBootstrapper.CORR) ? Integer.MAX_VALUE : IRelSampler.EMPTY;
		};

		var scheduledRules = matchContainer.streamAllRules()//
				.filter(r -> noCorrContext(toRule(r)) || latestCorrIDs.getIDs().size() > 0)//
				.collect(Collectors.toMap(Functions.identity(), //
						r -> new Schedule(-1, new ElementRange(), latestCorrIDs, r,
								(type, ruleName, nodeName) -> INodeSampler.EMPTY, sampler)));

		return scheduledRules;
	}
}
