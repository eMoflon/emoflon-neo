package org.emoflon.neo.engine.modules.matchreprocessors;

import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;

public class BWD_OPTReprocessor extends OPTReprocessor {

	public BWD_OPTReprocessor(TripleRuleAnalyser analyser) {
		super(analyser, true, true, false);
	}

}
