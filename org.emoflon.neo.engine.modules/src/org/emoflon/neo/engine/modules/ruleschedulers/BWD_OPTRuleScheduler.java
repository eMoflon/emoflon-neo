package org.emoflon.neo.engine.modules.ruleschedulers;

import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;

public class BWD_OPTRuleScheduler extends OPTRuleScheduler {

	public BWD_OPTRuleScheduler(TripleRuleAnalyser analyser) {
		super(analyser, true, true, false);
	}
}
