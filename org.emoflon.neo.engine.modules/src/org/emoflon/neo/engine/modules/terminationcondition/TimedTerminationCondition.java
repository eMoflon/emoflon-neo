package org.emoflon.neo.engine.modules.terminationcondition;

import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.ITerminationCondition;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

public class TimedTerminationCondition implements ITerminationCondition<NeoMatch, NeoCoMatch> {
	private long startTime;
	private long maxDuration;

	public TimedTerminationCondition(long pMaxDuration) {
		maxDuration = pMaxDuration;
		start();
	}

	public void start() {
		startTime = System.currentTimeMillis();
	}

	@Override
	public boolean isReached(MatchContainer<NeoMatch, NeoCoMatch> matchContainer) {
		if (System.currentTimeMillis() >= startTime + maxDuration)
			return true;
		else
			return false;
	}
}
