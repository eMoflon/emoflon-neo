package org.emoflon.neo.engine.modules.ruleschedulers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IRuleScheduler;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

public class AllRulesAllMatchesScheduler implements IRuleScheduler<NeoMatch, NeoCoMatch> {

	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Integer> scheduleWith(//
			Collection<IRule<NeoMatch, NeoCoMatch>> pAvailableRules, //
			IMonitor pProgressMonitor//
	) {
		var allRules = new HashMap<IRule<NeoMatch, NeoCoMatch>, Integer>();
		pAvailableRules.forEach(r -> allRules.put(r, -1));
		return allRules;
	}

}
