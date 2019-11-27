package org.emoflon.neo.engine.modules.valueGenerators;

import java.util.Random;

import org.emoflon.neo.emsl.eMSL.BuiltInDataTypes;
import org.emoflon.neo.emsl.eMSL.DataType;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.engine.generator.modules.IParameterValueGenerator;

public class RandomIntegerGenerator implements IParameterValueGenerator<DataType, Integer> {

	private Random rand;

	public RandomIntegerGenerator() {
		this(new Random());
	}

	public RandomIntegerGenerator(long seed) {
		this(new Random(seed));
	}

	public RandomIntegerGenerator(Random randomGenerator) {
		rand = randomGenerator;
	}

	@Override
	public boolean generatesValueFor(String parameterName, DataType dataType) {
		return EMSLUtil.castToBuiltInType(dataType)//
				.map(dt -> dt.equals(BuiltInDataTypes.EINT))//
				.orElse(false);
	}

	@Override
	public Integer generateValueFor(String parameterName, DataType dataType) {
		return rand.nextInt();
	}
}
