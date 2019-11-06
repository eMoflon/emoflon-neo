package org.emoflon.neo.cypher.rules;

import java.util.Optional;

import org.emoflon.neo.cypher.common.NeoMask;
import org.emoflon.neo.cypher.patterns.NeoData;
import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.patterns.NeoPattern;
import org.emoflon.neo.cypher.patterns.NeoPatternAccess;

public abstract class NeoRuleAccess<Data extends NeoData, Mask extends NeoMask> extends NeoPatternAccess<Data, Mask> {
	public abstract NeoRule rule();

	@Override
	public NeoPattern pattern() {
		return rule().getPrecondition();
	}

	public Optional<NeoCoMatch> apply(NeoMatch match, Mask mask) {
		return rule().apply(match, mask);
	}
	
	public Optional<NeoCoMatch> apply(Mask preMask, Mask postMask) {
		return rule().apply(preMask, postMask);
	}
}
