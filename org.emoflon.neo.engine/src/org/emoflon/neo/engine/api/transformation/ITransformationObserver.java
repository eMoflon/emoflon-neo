package org.emoflon.neo.engine.api.transformation;

public interface ITransformationObserver {
	void notifyTransformationStart();

	void notifyTransformationStop();
}
