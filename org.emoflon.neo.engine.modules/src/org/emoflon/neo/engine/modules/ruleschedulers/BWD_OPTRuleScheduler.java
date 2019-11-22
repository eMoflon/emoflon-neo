package org.emoflon.neo.engine.modules.ruleschedulers;

import java.util.Collection;

import org.emoflon.neo.emsl.eMSL.TripleRule;

public class BWD_OPTRuleScheduler extends OPTRuleScheduler {

	public BWD_OPTRuleScheduler(Collection<TripleRule> tripleRules) {
		super(tripleRules, true, true, false);
	}
}
