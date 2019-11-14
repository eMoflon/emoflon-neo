package org.emoflon.neo.engine.modules.cleanup;

import org.emoflon.neo.engine.generator.modules.ICleanupModule;

public class NoOpCleanup implements ICleanupModule {
	@Override
	public void cleanup() {
		// no-op
	}

	@Override
	public String description() {
		return "No cleanup operations";
	}
}
