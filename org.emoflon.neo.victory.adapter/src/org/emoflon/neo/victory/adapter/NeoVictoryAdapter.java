package org.emoflon.neo.victory.adapter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.emoflon.ibex.tgg.ui.debug.api.DataProvider;
import org.emoflon.ibex.tgg.ui.debug.api.Rule;
import org.emoflon.neo.emsl.eMSL.TripleRule;

public class NeoVictoryAdapter implements DataProvider {
	private Collection<NeoRuleAdapter> rules;

	public NeoVictoryAdapter(Collection<org.emoflon.neo.emsl.eMSL.Rule> operationalRules, Collection<TripleRule> tripleRules) {
		var tripleRuleItr = tripleRules.iterator();
		this.rules = operationalRules.stream()//
				.map(r -> new NeoRuleAdapter(r, tripleRuleItr.next()))//
				.collect(Collectors.toList());
	}

	@Override
	public Collection<Rule> getAllRules() {
		return Collections.unmodifiableCollection(rules);
	}

	@Override
	public void saveModels() throws IOException {
		// TODO Not sure what to do here? Nothing? All models are always "saved" in the
		// database without any extra effort.
	}
}
