package org.emoflon.neo.neo4j.adapter;

import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.api.rules.IRule;

public class NeoMatchValid implements IMatch {

	public NeoMatchValid() {

	}

	@Override
	public IRule getRule() {
		return null;
	}

	@Override
	public boolean isStillValid() {
		return true;
	}

	@Override
	public void destroy() {
	}

}
