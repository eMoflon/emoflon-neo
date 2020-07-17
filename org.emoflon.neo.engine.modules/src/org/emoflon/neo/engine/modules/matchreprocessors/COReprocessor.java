package org.emoflon.neo.engine.modules.matchreprocessors;

import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;

public class COReprocessor extends OPTReprocessor {

	public COReprocessor(TripleRuleAnalyser analyser) {
		super(analyser, false, false, false);
	}

}
