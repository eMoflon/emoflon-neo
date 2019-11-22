package org.emoflon.neo.engine.modules;

import java.util.Collection;
import java.util.stream.Stream;

import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.cypher.rules.NeoRule;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.MatchContainer;

public class NeoMatchContainer extends MatchContainer<NeoMatch, NeoCoMatch> {

	public NeoMatchContainer(Collection<? extends IRule<NeoMatch, NeoCoMatch>> allRules) {
		super(allRules);
	}

	@Override
	public void appliedRule(IRule<NeoMatch, NeoCoMatch> rule, Collection<NeoMatch> appliedMatches,
			Collection<NeoCoMatch> comatches) {
		int noOfApplications = appliedMatches.size();
		var before = ruleApplications.get(rule);
		var after = before + noOfApplications;
		ruleApplications.put(rule, after);

		comatches.forEach(cm -> addCreatedElementIDsToRange(cm, rule));
		this.coMatches.addAll(comatches);
	}

	@Override
	protected Stream<String> getTypesForNode(IRule<NeoMatch, NeoCoMatch> rule, String nodeName) {
		var neoRule = (NeoRule) rule;
		return neoRule.getCreatedNodes().get(nodeName).getLabels().stream();
	}

	@Override
	protected String getTypeForRel(IRule<NeoMatch, NeoCoMatch> rule, String relNameAccordingToConvention) {
		var neoRule = (NeoRule) rule;
		return neoRule.getCreatedEdges().get(relNameAccordingToConvention).getType();
	}

}
