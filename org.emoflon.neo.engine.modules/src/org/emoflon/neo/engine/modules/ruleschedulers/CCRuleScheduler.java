package org.emoflon.neo.engine.modules.ruleschedulers;

import java.util.Collection;

import org.emoflon.neo.emsl.eMSL.TripleRule;

public class CCRuleScheduler extends OPTRuleScheduler {

	public CCRuleScheduler(Collection<TripleRule> tripleRules) {
		super(tripleRules, false, true, false);
	}
}
