package org.emoflon.neo.engine.generator;

@FunctionalInterface
public interface INodeSampler {
	int EMPTY = -1;

	int getSampleSizeFor(String type, String ruleName, String nodeName);
}
