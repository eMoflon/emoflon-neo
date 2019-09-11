package org.emoflon.neo.engine.modules;

import org.emoflon.neo.engine.generator.ITerminationCondition;

public class TimedTerminationCondition implements ITerminationCondition {

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
	public boolean isReached() {
		if (System.currentTimeMillis() >= startTime + maxDuration)
			return true;
		else
			return false;
	}
}
