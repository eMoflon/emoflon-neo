package org.emoflon.neo.engine.modules.ilp;

import java.util.Collection;
import java.util.HashMap;

import org.emoflon.neo.engine.api.rules.ICoMatch;

public class CO extends OPT {

	public CO(Collection<ICoMatch> matches) {
		super(matches);
	}

	@Override
	protected void computeWeights() {
		// TODO[Anjorin] Compute weights for matches (matchToWeight)
		// - Number of "green" elements in the match
		matchToWeight = new HashMap<>();
		
		for (var m : matchToId.keySet()) {
			
		}
	}
}
