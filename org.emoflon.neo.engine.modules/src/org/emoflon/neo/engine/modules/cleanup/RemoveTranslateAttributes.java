package org.emoflon.neo.engine.modules.cleanup;

import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.generator.modules.ICleanupModule;

public class RemoveTranslateAttributes implements ICleanupModule {

	private NeoCoreBuilder builder;
	private String modelName;

	public RemoveTranslateAttributes(NeoCoreBuilder builder, String modelName) {
		this.builder = builder;
		this.modelName = modelName;
	}

	@Override
	public void cleanup() {
		builder.removeTranslateAttributesFromModel(modelName);
	}

	@Override
	public String description() {
		return "Removing the internal translate-attribute (_tr_) from all model elements.";
	}
}
