package org.emoflon.neo.engine.modules.matchreprocessors;

import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;

public class FWD_OPTReprocessor extends OPTReprocessor {

	public FWD_OPTReprocessor(TripleRuleAnalyser analyser) {
		super(analyser, false, true, true);
	}

}
