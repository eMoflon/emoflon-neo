package org.emoflon.neo.engine.modules.valueGenerators;

import org.emoflon.neo.engine.generator.modules.IParameterValueGenerator;

public class StringValueGenerator implements IParameterValueGenerator {

	@Override
	public Object generateValueFor(String parameterName) {
		return "value";
	}

}
