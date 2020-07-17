package org.emoflon.neo.engine.modules.startup;

import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.generator.modules.IStartupModule;

public class PrepareContextDeltaAttributes implements IStartupModule {

	private NeoCoreBuilder builder;
	private String src;
	private String trg;

	public PrepareContextDeltaAttributes(NeoCoreBuilder builder, String src, String trg) {
		this.builder = builder;
		this.src = src;
		this.trg = trg;
	}

	@Override
	public void startup() {
		builder.prepareModelWithContextDeltaAttribute(src, trg);
	}

	@Override
	public String description() {
		return "Setting the internal existance attribute (_ex_) to true for all existing elements outside create and delete delta.";
	}
}
