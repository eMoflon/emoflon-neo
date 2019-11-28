package org.emoflon.neo.engine.modules.matchreprocessors;

import static org.emoflon.neo.engine.modules.analysis.RuleAnalyser.hasRelevantContext;

import java.util.ArrayList;
import java.util.Collection;

import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.emsl.eMSL.TripleRule;
import org.emoflon.neo.emsl.refinement.EMSLFlattener;
import org.emoflon.neo.emsl.util.FlattenerException;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.IMatchReprocessor;
import org.emoflon.neo.engine.generator.modules.IMonitor;

/**
 * This reprocessor exploits the fact that rules with no corr context can be
 * removed after the first iteration of CC (as all their matches must have been
 * collected already).
 * 
 * @author aanjorin
 */
public abstract class OPTReprocessor implements IMatchReprocessor<NeoMatch, NeoCoMatch> {

	private Collection<TripleRule> tripleRules;
	private boolean SRC;
	private boolean CORR;
	private boolean TRG;
	private boolean firstIteration = true;

	public OPTReprocessor(Collection<TripleRule> tripleRules, boolean SRC, boolean CORR, boolean TRG) {
		this.SRC = SRC;
		this.CORR = CORR;
		this.TRG = TRG;
		
		this.tripleRules = new ArrayList<>();
		for (var tripleRule : tripleRules) {
			try {
				var flatTr = (TripleRule) EMSLFlattener.flatten(tripleRule);
				this.tripleRules.add(flatTr);
			} catch (FlattenerException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void reprocess(MatchContainer<NeoMatch, NeoCoMatch> matchContainer,
			IMonitor<NeoMatch, NeoCoMatch> progressMonitor) {
		if (firstIteration) {
			if (matchContainer.getNumberOfRuleApplications() > 0) {
				var rulesWithMatches = matchContainer.getAllRulesToMatches().keySet();
				rulesWithMatches.stream()//
						.filter(r -> !hasRelevantContext(r.getName(), tripleRules, SRC, CORR, TRG))//
						.forEach(matchContainer::removeRule);
			}

			firstIteration = false;
		}

		matchContainer.clear();
	}
}
