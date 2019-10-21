package org.emoflon.neo.engine.modules.valueGenerators;

import org.emoflon.neo.emsl.eMSL.BuiltInDataTypes;
import org.emoflon.neo.emsl.eMSL.DataType;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.engine.generator.modules.IParameterValueGenerator;

public class LoremIpsumStringValueGenerator implements IParameterValueGenerator<DataType, String> {

	@Override
	public String generateValueFor(String parameterName) {
		return LoremIpsum.getInstance().randomWord();
	}

	@Override
	public boolean generatesValueFor(String parameterName, DataType dataType) {
		return EMSLUtil.castToBuiltInType(dataType)//
				.map(dt -> dt.equals(BuiltInDataTypes.ESTRING))//
				.orElse(false);
	}

}
