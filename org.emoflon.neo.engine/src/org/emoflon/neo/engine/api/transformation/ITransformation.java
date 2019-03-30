package org.emoflon.neo.engine.api.transformation;

public interface ITransformation {
	void registerObserver(ITransformationObserver observer);

	void setPolicy(ITransformationPolicy policy);

	void run();
}
