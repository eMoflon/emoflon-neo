package org.emoflon.neo.engine.modules.valueGenerators;

import java.util.UUID;

import org.emoflon.neo.emsl.eMSL.BuiltInDataTypes;
import org.emoflon.neo.emsl.eMSL.DataType;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.engine.generator.modules.IParameterValueGenerator;

public class RandomUUIDGenerator implements IParameterValueGenerator<DataType, String> {

	@Override
	public String generateValueFor(String parameterName, DataType dataType) {
		return UUID.randomUUID().toString();
	}

	@Override
	public boolean generatesValueFor(String parameterName, DataType dataType) {
		return EMSLUtil.castToBuiltInType(dataType)//
				.map(dt -> dt.equals(BuiltInDataTypes.ESTRING))//
				.orElse(false);
	}
}
