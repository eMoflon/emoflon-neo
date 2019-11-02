package org.emoflon.neo.engine.modules;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoRule;

public class NeoMatchContainer extends MatchContainer<NeoMatch, NeoCoMatch> {

	public NeoMatchContainer(Collection<? extends IRule<NeoMatch, NeoCoMatch>> allRules) {
		super(allRules);
	}

	@Override
	public void appliedRule(IRule<NeoMatch, NeoCoMatch> rule, Collection<NeoMatch> appliedMatches,
			Optional<Collection<NeoCoMatch>> comatches) {
		int noOfApplications = appliedMatches.size();
		var before = ruleApplications.get(rule);
		var after = before + noOfApplications;
		ruleApplications.put(rule, after);

		comatches.ifPresent(cms -> cms.forEach(cm -> addCreatedElementIDsToRange(cm, rule)));
	}

	@Override
	protected Stream<String> getTypesFor(IRule<NeoMatch, NeoCoMatch> rule, String elt) {
		var neoRule = (NeoRule) rule;
		return neoRule.getCreatedNodes().get(elt).getLabels().stream();
	}

}
