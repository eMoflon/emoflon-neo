package org.emoflon.neo.engine.modules.ruleschedulers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.INodeSampler;
import org.emoflon.neo.engine.generator.IRelSampler;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.Schedule;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IRuleScheduler;
import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;

import com.google.common.base.Functions;

/**
 * This scheduler exploits the fact that rules with created context can never match
 * for OPT if there were no elements created in the last step (they must have been
 * collected already). An exception is the first collection step, as this treats
 * only the source and target model nodes.
 * 
 * @author aanjorin
 */
public class OPTRuleScheduler implements IRuleScheduler<NeoMatch, NeoCoMatch> {

	protected Collection<Object> allRelIDsUpToLastStep = new ArrayList<>();
	protected Collection<Object> allNodeIDsUpToLastStep = new ArrayList<>();
	protected boolean SRC;
	protected boolean CORR;
	protected boolean TRG;
	protected int iteration = 0;
	protected TripleRuleAnalyser analyser;

	public OPTRuleScheduler(TripleRuleAnalyser analyser, boolean SRC, boolean CORR, boolean TRG) {
		this.SRC = SRC;
		this.CORR = CORR;
		this.TRG = TRG;
		this.analyser = analyser;
	}

	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Schedule> scheduleWith(//
			MatchContainer<NeoMatch, NeoCoMatch> matchContainer, //
			IMonitor<NeoMatch, NeoCoMatch> progressMonitor//
	) {
		iteration++;
		
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
		
		// source and target model
//		if (iteration == 1) {
//			return matchContainer.getAllRulesToMatches().entrySet().stream()//
//					.collect(Collectors.toMap(Entry::getKey, entry -> Schedule.unlimited()));
//		}
		
		// axioms
		if (iteration == 1) {
			return matchContainer.getAllRulesToMatches().entrySet().stream()//
					.collect(Collectors.toMap(Entry::getKey, entry -> Schedule.unlimited()));
		}
		
		// other rules
		return matchContainer.streamAllRules()//
				.filter(r -> !analyser.hasRelevantContext(r.getName(), SRC, CORR, TRG)
						|| latestRelIDs.getIDs().size() > 0 || latestNodeIDs.getIDs().size() > 0)
				.collect(Collectors.toMap(Functions.identity(), //
						r -> new Schedule(-1, latestNodeIDs, latestRelIDs, r, nodeSampler, relSampler)));
	}
}
