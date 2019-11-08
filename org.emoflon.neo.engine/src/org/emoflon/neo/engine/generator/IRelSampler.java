package org.emoflon.neo.engine.generator;

@FunctionalInterface
public interface IRelSampler {
	int EMPTY = -1;

	int getSampleSizeFor(String type, String ruleName);
}
