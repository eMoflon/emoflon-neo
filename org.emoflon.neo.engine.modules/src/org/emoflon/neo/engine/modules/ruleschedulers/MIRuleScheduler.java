package org.emoflon.neo.engine.modules.ruleschedulers;

import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;

public class MIRuleScheduler extends OPTRuleScheduler {

	public MIRuleScheduler(TripleRuleAnalyser analyser) {
		super(analyser, true, true, true);
	}
}
