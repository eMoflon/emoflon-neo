package org.emoflon.neo.engine.generator;

public interface IMonitor {
	public enum GeneratorStep {
		RULE_SCHEDULING, MATCH_SELECTION, MATCH_REPROCESSING;
	}

	public void start(GeneratorStep pStep);

	public void finish(GeneratorStep pStep);
}
