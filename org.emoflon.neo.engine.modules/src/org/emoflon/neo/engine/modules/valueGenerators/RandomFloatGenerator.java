package org.emoflon.neo.engine.modules.valueGenerators;

import java.util.Random;

import org.emoflon.neo.emsl.eMSL.BuiltInDataTypes;
import org.emoflon.neo.emsl.eMSL.DataType;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.engine.generator.modules.IParameterValueGenerator;

public class RandomFloatGenerator implements IParameterValueGenerator<DataType, Float> {

	private Random rand;

	public RandomFloatGenerator() {
		this(new Random());
	}

	public RandomFloatGenerator(long seed) {
		this(new Random(seed));
	}

	public RandomFloatGenerator(Random randomGenerator) {
		rand = randomGenerator;
	}

	@Override
	public boolean generatesValueFor(String parameterName, DataType dataType) {
		return EMSLUtil.castToBuiltInType(dataType)//
				.map(dt -> dt.equals(BuiltInDataTypes.EFLOAT))//
				.orElse(false);
	}

	@Override
	public Float generateValueFor(String parameterName) {
		return rand.nextFloat();
	}
}
