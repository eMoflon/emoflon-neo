package org.emoflon.neo.engine.modules.matchreprocessors;

import java.util.Collection;

import org.emoflon.neo.emsl.eMSL.TripleRule;

public class CCReprocessor extends OPTReprocessor {

	public CCReprocessor(Collection<TripleRule> tripleRules) {
		super(tripleRules, false, true, false);
	}

}
