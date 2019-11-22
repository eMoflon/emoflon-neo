package org.emoflon.neo.engine.modules.terminationcondition;

import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.ITerminationCondition;

public class CounterTerminationCondition implements ITerminationCondition<NeoMatch, NeoCoMatch> {

	private int nrOfRepetitions;
	private int counter;
	
	public CounterTerminationCondition(int nrOfRepetitions) {
		this.nrOfRepetitions = nrOfRepetitions;
		counter = 0;
	}
	
	@Override
	public boolean isReached(MatchContainer<NeoMatch, NeoCoMatch> matchContainer) {
		if (++counter >= nrOfRepetitions) {
			return true;
		} else
			return false;
	}

}
