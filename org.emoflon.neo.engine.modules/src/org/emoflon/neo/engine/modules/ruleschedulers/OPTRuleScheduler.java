package org.emoflon.neo.engine.modules.ruleschedulers;

import static org.emoflon.neo.engine.modules.analysis.RuleAnalyser.noRelevantContext;
import static org.emoflon.neo.engine.modules.analysis.RuleAnalyser.toTripleRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.emsl.eMSL.TripleRule;
import org.emoflon.neo.engine.api.rules.IRule;
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
public class OPTRuleScheduler implements IRuleScheduler<NeoMatch, NeoCoMatch> {

	private Collection<Object> allRelIDsUpToLastStep = new ArrayList<>();
	private Collection<Object> allNodeIDsUpToLastStep = new ArrayList<>();
	private Collection<TripleRule> tripleRules;
	private boolean SRC;
	private boolean CORR;
	private boolean TRG;
	
	public OPTRuleScheduler(Collection<TripleRule> tripleRules, boolean SRC, boolean CORR, boolean TRG) {
		this.tripleRules = tripleRules;
		this.SRC = SRC;
		this.CORR = CORR;
		this.TRG = TRG;
	}

	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Schedule> scheduleWith(//
			MatchContainer<NeoMatch, NeoCoMatch> matchContainer, //
			IMonitor<NeoMatch, NeoCoMatch> progressMonitor//
	) {
		var latestRelIDs = matchContainer.getRelRange().remove(allRelIDsUpToLastStep);
		allRelIDsUpToLastStep = matchContainer.getRelRange().getIDs();

		var latestNodeIDs = matchContainer.getNodeRange().remove(allNodeIDsUpToLastStep);
		allNodeIDsUpToLastStep = matchContainer.getNodeRange().getIDs();

		IRelSampler relSampler = (type, ruleName) -> {
			return latestRelIDs.getTypes().contains(type) ? Integer.MAX_VALUE : IRelSampler.EMPTY;
		};

		INodeSampler nodeSampler = (type, ruleName, nodeName) -> {
			return latestNodeIDs.getTypes().contains(type) ? Integer.MAX_VALUE : IRelSampler.EMPTY;
		};

		var scheduledRules = matchContainer.streamAllRules()//
				.filter(r -> noRelevantContext(toTripleRule(r.getName(), tripleRules), SRC, CORR, TRG) || latestRelIDs.getIDs().size() > 0
						|| latestNodeIDs.getIDs().size() > 0)//
				.collect(Collectors.toMap(Functions.identity(), //
						r -> new Schedule(-1, latestNodeIDs, latestRelIDs, r, nodeSampler, relSampler)));

		return scheduledRules;
	}
}
