package org.emoflon.neo.engine.modules.matchreprocessors;

import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;

public class MIReprocessor extends OPTReprocessor {

	public MIReprocessor(TripleRuleAnalyser analyser) {
		super(analyser, true, true, true);
	}
}
