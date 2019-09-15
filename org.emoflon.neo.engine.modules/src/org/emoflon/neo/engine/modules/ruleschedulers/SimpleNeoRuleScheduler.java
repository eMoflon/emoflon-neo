package org.emoflon.neo.engine.modules.ruleschedulers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IRuleScheduler;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

/**
 * Asks for 10 matches of each rule.
 */
public class SimpleNeoRuleScheduler implements IRuleScheduler<NeoMatch, NeoCoMatch> {

	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Integer> scheduleWith(
			Collection<IRule<NeoMatch, NeoCoMatch>> pAvailableRules, IMonitor pProgressMonitor) {
		Map<IRule<NeoMatch, NeoCoMatch>, Integer> scheduleMap = new HashMap<>();
		pAvailableRules.forEach(rule -> scheduleMap.put(rule, 10));
		return scheduleMap;
	}
}
