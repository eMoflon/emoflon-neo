package org.emoflon.neo.engine.modules.startup;

import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.generator.modules.IStartupModule;

public class PrepareTranslateAttributes implements IStartupModule {

	private NeoCoreBuilder builder;
	private String modelName;

	public PrepareTranslateAttributes(NeoCoreBuilder builder, String modelName) {
		this.builder = builder;
		this.modelName = modelName;
	}

	@Override
	public void startup() {
		builder.prepareModelWithTranslateAttribute(modelName);
	}

	@Override
	public String description() {
		return "Adding the internal translate-attribute (_tr_) to all model elements.";
	}
}
