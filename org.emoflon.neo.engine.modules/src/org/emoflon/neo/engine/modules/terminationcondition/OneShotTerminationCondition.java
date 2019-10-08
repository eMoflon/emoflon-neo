package org.emoflon.neo.engine.modules.terminationcondition;

import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.ITerminationCondition;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

public class OneShotTerminationCondition implements ITerminationCondition<NeoMatch, NeoCoMatch> {

	private boolean done = false;

	@Override
	public boolean isReached(MatchContainer<NeoMatch, NeoCoMatch> matchContainer) {
		if (!done) {
			done = true;
			return false;
		} else
			return true;
	}

}
