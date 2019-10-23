package org.emoflon.neo.victory.adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.emoflon.neo.emsl.eMSL.TripleRule;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.Generator;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IUpdatePolicy;
import org.emoflon.neo.neo4j.adapter.models.NeoCoreBuilder;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;
import org.emoflon.victory.ui.api.DataProvider;
import org.emoflon.victory.ui.api.Rule;
import org.emoflon.victory.ui.api.Victory;

public class NeoVictoryAdapter implements DataProvider, IUpdatePolicy<NeoMatch, NeoCoMatch> {
	private Collection<NeoRuleAdapter> rules;
	private NeoCoreBuilder builder;
	private Victory victory;

	public NeoVictoryAdapter(NeoCoreBuilder builder, Collection<org.emoflon.neo.emsl.eMSL.Rule> operationalRules,
			Collection<TripleRule> tripleRules) {
		this.builder = builder;
		var tripleRuleItr = tripleRules.iterator();
		this.rules = operationalRules.stream()//
				.map(r -> new NeoRuleAdapter(r, tripleRuleItr.next()))//
				.collect(Collectors.toList());
		victory = new Victory();
	}

	@Override
	public Collection<Rule> getAllRules() {
		return Collections.unmodifiableCollection(rules);
	}

	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Collection<NeoMatch>> selectMatches(
			MatchContainer<NeoMatch, NeoCoMatch> matches, IMonitor<NeoMatch, NeoCoMatch> pProgressMonitor) {
		var selection = new ArrayList<NeoMatch>();
		var selected = (NeoMatchAdapter) victory.selectMatch(new NeoDataPackageAdapter(builder, matches, rules));
		selection.add((NeoMatch) selected.getWrappedMatch());

		var ruleName = selected.getWrappedMatch().getPattern().getName();
		var rule = matches.getAllRulesToMatches().keySet().stream().filter(r -> r.getName().equals(ruleName)).findAny();

		return Map.of(rule.get(), selection);
	}

	public void run(Generator<NeoMatch, NeoCoMatch> generator) {
		var matchProvider = new Thread("Match provider thread") {
			@Override
			public void run() {
				generator.generate();
			};
		};

		victory.run(this, matchProvider);
	}

	@Override
	public void saveModels(String[] saveLocations) throws IOException {
		// Database is always persistent
	}

	@Override
	public String[][] getDefaultSaveData() {
		// Database is always persistent
		return null;
	}
}
