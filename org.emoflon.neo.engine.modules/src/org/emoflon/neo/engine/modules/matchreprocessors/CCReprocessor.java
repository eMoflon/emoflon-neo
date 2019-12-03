package org.emoflon.neo.engine.modules.matchreprocessors;

import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;

public class CCReprocessor extends OPTReprocessor {

	public CCReprocessor(TripleRuleAnalyser analyser) {
		super(analyser, false, true, false);
	}

}
