package org.emoflon.neo.engine.modules.valueGenerators;

import java.util.Random;

import org.emoflon.neo.emsl.eMSL.BuiltInDataTypes;
import org.emoflon.neo.emsl.eMSL.DataType;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.engine.generator.modules.IParameterValueGenerator;

public class RandomLongGenerator implements IParameterValueGenerator<DataType, Long> {

	private Random rand;

	public RandomLongGenerator() {
		this(new Random());
	}

	public RandomLongGenerator(long seed) {
		this(new Random(seed));
	}

	public RandomLongGenerator(Random randomGenerator) {
		rand = randomGenerator;
	}

	@Override
	public boolean generatesValueFor(String parameterName, DataType dataType) {
		return EMSLUtil.castToBuiltInType(dataType)//
				.map(dt -> dt.equals(BuiltInDataTypes.ELONG))//
				.orElse(false);
	}

	@Override
	public Long generateValueFor(String parameterName, DataType dataType) {
		return rand.nextLong();
	}
}
