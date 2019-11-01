package org.emoflon.neo.engine.modules.terminationcondition;

import java.util.concurrent.TimeUnit;

import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.ITerminationCondition;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

public class TimedTerminationCondition implements ITerminationCondition<NeoMatch, NeoCoMatch> {
	private long startTime;
	private long maxDurationInMS;

	public TimedTerminationCondition(long maxDuration, TimeUnit unit) {
		switch (unit) {
		case MILLISECONDS:
			this.maxDurationInMS = maxDuration;
			break;
		case SECONDS:
			this.maxDurationInMS = maxDuration * 1000;
			break;
		case MINUTES:
			this.maxDurationInMS = maxDuration * 60 * 1000;
			break;
		case HOURS:
			this.maxDurationInMS = maxDuration * 60 * 60 * 1000;
			break;
		case DAYS:
			this.maxDurationInMS = maxDuration * 24 * 60 * 60 * 1000;
			break;
		default:
			throw new IllegalArgumentException("Unexpected value: " + unit);
		}

		start();
	}

	public void start() {
		startTime = System.currentTimeMillis();
	}

	@Override
	public boolean isReached(MatchContainer<NeoMatch, NeoCoMatch> matchContainer) {
		if (System.currentTimeMillis() >= startTime + maxDurationInMS)
			return true;
		else
			return false;
	}
}
