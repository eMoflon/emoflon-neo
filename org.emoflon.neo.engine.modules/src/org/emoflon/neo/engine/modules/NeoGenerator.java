package org.emoflon.neo.engine.modules;

import java.util.Collection;

import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.Generator;
import org.emoflon.neo.engine.generator.modules.IMatchReprocessor;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IRuleScheduler;
import org.emoflon.neo.engine.generator.modules.ITerminationCondition;
import org.emoflon.neo.engine.generator.modules.IUpdatePolicy;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

public class NeoGenerator extends Generator<NeoMatch, NeoCoMatch> {

	public NeoGenerator(Collection<IRule<NeoMatch, NeoCoMatch>> allRules, ITerminationCondition<NeoMatch, NeoCoMatch> terminationCondition,
			IRuleScheduler<NeoMatch, NeoCoMatch> ruleScheduler, IUpdatePolicy<NeoMatch, NeoCoMatch> updatePolicy,
			IMatchReprocessor<NeoMatch, NeoCoMatch> matchReprocessor, IMonitor progressMonitor) {
		super(allRules, terminationCondition, ruleScheduler, updatePolicy, matchReprocessor, progressMonitor);
	}

}
