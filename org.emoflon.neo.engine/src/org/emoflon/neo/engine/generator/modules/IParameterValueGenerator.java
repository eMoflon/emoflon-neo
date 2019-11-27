package org.emoflon.neo.engine.generator.modules;

public interface IParameterValueGenerator<T, V> {
	public boolean generatesValueFor(String parameterName, T dataType);

	public V generateValueFor(String parameterName, T dataType);
}
