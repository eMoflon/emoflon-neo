package org.emoflon.neo.engine.modules.ruleschedulers;

import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;

public class FWD_OPTRuleScheduler extends OPTRuleScheduler {

	public FWD_OPTRuleScheduler(TripleRuleAnalyser analyser) {
		super(analyser, false, true, true);
	}
}
