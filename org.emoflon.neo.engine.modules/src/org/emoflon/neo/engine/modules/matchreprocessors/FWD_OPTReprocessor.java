package org.emoflon.neo.engine.modules.matchreprocessors;

import java.util.Collection;

import org.emoflon.neo.emsl.eMSL.TripleRule;

public class FWD_OPTReprocessor extends OPTReprocessor {

	public FWD_OPTReprocessor(Collection<TripleRule> tripleRules) {
		super(tripleRules, false, true, true);
	}

}
