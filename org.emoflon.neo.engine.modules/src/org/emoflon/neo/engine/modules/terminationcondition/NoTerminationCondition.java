package org.emoflon.neo.engine.modules.terminationcondition;

import org.emoflon.neo.engine.generator.modules.ITerminationCondition;

public class NoTerminationCondition implements ITerminationCondition {

	@Override
	public boolean isReached() {
		return false;
	}

}
