package org.emoflon.neo.engine.modules.terminationcondition;

import org.emoflon.neo.engine.generator.modules.ITerminationCondition;

public class OneShotTerminationCondition implements ITerminationCondition {

	private boolean done = false;

	@Override
	public boolean isReached() {
		if (!done) {
			done = true;
			return false;
		} else
			return true;
	}

}
