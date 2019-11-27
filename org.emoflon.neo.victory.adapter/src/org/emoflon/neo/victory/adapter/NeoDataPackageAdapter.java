package org.emoflon.neo.victory.adapter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.victory.adapter.matches.NeoMatchAdapter;
import org.emoflon.neo.victory.adapter.rules.NeoRuleAdapter;
import org.emoflon.victory.ui.api.DataPackage;
import org.emoflon.victory.ui.api.Match;
import org.emoflon.victory.ui.api.RuleApplication;

public class NeoDataPackageAdapter implements DataPackage {
	private MatchContainer<NeoMatch, NeoCoMatch> matches;
	private Collection<NeoRuleAdapter> rules;
	private NeoCoreBuilder builder;

	public NeoDataPackageAdapter(NeoCoreBuilder builder, MatchContainer<NeoMatch, NeoCoMatch> matches,
			Collection<NeoRuleAdapter> rules) {
		this.matches = matches;
		this.rules = rules;
		this.builder = builder;
	}

	@Override
	public Collection<Match> getMatches() {
		return matches.streamAllMatches().map(m -> new NeoMatchAdapter(builder, m, rules)).collect(Collectors.toList());
	}

	@Override
	public List<RuleApplication> getRuleApplications() {
		return Collections.emptyList();
	}

}
