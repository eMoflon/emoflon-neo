package org.emoflon.neo.engine.modules.matchreprocessors;

import static org.emoflon.neo.engine.modules.analysis.RuleAnalyser.noRelevantContext;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.emsl.eMSL.TripleRule;
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
		this.tripleRules = tripleRules;
	}

	@Override
	public void reprocess(MatchContainer<NeoMatch, NeoCoMatch> matchContainer,
			IMonitor<NeoMatch, NeoCoMatch> progressMonitor) {
		if (firstIteration) {
			if (matchContainer.getNumberOfRuleApplications() > 0) {
				var rulesWithMatches = matchContainer.getAllRulesToMatches().keySet();
				var namesToRules = rulesWithMatches.stream()//
						.collect(Collectors.toMap(r -> r.getName(), Function.identity()));
				tripleRules.stream()//
						.filter(r -> namesToRules.containsKey(r.getName()))//
						.filter(r -> noRelevantContext(r, SRC, CORR, TRG))//
						.map(r -> namesToRules.get(r.getName()))//
						.forEach(matchContainer::removeRule);
			}

			firstIteration = false;
		}

		matchContainer.clear();
	}
}
