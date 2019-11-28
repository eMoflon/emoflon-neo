package org.emoflon.neo.engine.modules.ruleschedulers;

import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;

public class CCRuleScheduler extends OPTRuleScheduler {

	public CCRuleScheduler(TripleRuleAnalyser analyser) {
		super(analyser, false, true, false);
	}
}
