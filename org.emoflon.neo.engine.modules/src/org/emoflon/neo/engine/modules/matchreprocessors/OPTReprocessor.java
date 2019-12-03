package org.emoflon.neo.engine.modules.matchreprocessors;

import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.IMatchReprocessor;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;

/**
 * This reprocessor exploits the fact that rules with no corr context can be
 * removed after the first iteration of CC (as all their matches must have been
 * collected already).
 * 
 * @author aanjorin
 */
public abstract class OPTReprocessor implements IMatchReprocessor<NeoMatch, NeoCoMatch> {
	private boolean SRC;
	private boolean CORR;
	private boolean TRG;
	private boolean firstIteration = true;
	private TripleRuleAnalyser analyser;

	public OPTReprocessor(TripleRuleAnalyser analyser, boolean SRC, boolean CORR, boolean TRG) {
		this.SRC = SRC;
		this.CORR = CORR;
		this.TRG = TRG;
		this.analyser = analyser;
	}

	@Override
	public void reprocess(MatchContainer<NeoMatch, NeoCoMatch> matchContainer,
			IMonitor<NeoMatch, NeoCoMatch> progressMonitor) {
		if (firstIteration) {
			if (matchContainer.getNumberOfRuleApplications() > 0) {
				var rulesWithMatches = matchContainer.getAllRulesToMatches().keySet();
				rulesWithMatches.stream()//
						.filter(r -> !analyser.hasRelevantContext(r.getName(), SRC, CORR, TRG))//
						.forEach(matchContainer::removeRule);
			}

			firstIteration = false;
		}

		matchContainer.clear();
	}
}
