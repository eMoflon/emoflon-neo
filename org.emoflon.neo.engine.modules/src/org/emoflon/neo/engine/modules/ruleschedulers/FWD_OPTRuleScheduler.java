package org.emoflon.neo.engine.modules.ruleschedulers;

import java.util.Collection;

import org.emoflon.neo.emsl.eMSL.TripleRule;

public class FWD_OPTRuleScheduler extends OPTRuleScheduler {

	public FWD_OPTRuleScheduler(Collection<TripleRule> tripleRules) {
		super(tripleRules, false, true, true);
	}
}
