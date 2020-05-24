package org.emoflon.neo.engine.generator.modules;

public interface IParameterValueGenerator<T, V> {
	public boolean generatesValueFor(String parameterName, T dataType);
	public default boolean generatesValue(T dataType) {
		return generatesValueFor("_", dataType);
	}
	
	public V generateValueFor(String parameterName, T dataType);
	public default V generateValue(T dataType) {
		return generateValueFor("_", dataType);
	}
}
