package org.emoflon.neo.engine.modules.matchreprocessors;

import java.util.Collection;

import org.emoflon.neo.emsl.eMSL.TripleRule;

public class BWD_OPTReprocessor extends OPTReprocessor {

	public BWD_OPTReprocessor(Collection<TripleRule> tripleRules) {
		super(tripleRules, true, true, false);
	}

}
