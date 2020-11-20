package org.emoflon.neo.engine.modules.matchreprocessors;

import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;

/**
 * This reprocessor exploits the fact that rules with no corr context can be
 * removed after the first iteration of CC (as all their matches must have been
 * collected already).
 * 
 * @author aanjorin
 */
public class CCReprocessor extends OPTReprocessor {
	
	public CCReprocessor(TripleRuleAnalyser analyser) {
		super(analyser, false, true, false);
	}
}

