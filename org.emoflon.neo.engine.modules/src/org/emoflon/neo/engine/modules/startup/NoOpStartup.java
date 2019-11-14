package org.emoflon.neo.engine.modules.startup;

import org.emoflon.neo.engine.generator.modules.IStartupModule;

public class NoOpStartup implements IStartupModule {
	@Override
	public void startup() {
		// no-op
	}

	@Override
	public String description() {
		return "No startup operations";
	}
}
