package org.emoflon.neo.engine.modules.valueGenerators;

import java.util.Random;

import org.emoflon.neo.emsl.eMSL.BuiltInDataTypes;
import org.emoflon.neo.emsl.eMSL.DataType;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.engine.generator.modules.IParameterValueGenerator;

public class RandomDoubleGenerator implements IParameterValueGenerator<DataType, Double> {

	private Random rand;

	public RandomDoubleGenerator() {
		this(new Random());
	}

	public RandomDoubleGenerator(long seed) {
		this(new Random(seed));
	}

	public RandomDoubleGenerator(Random randomGenerator) {
		rand = randomGenerator;
	}

	@Override
	public boolean generatesValueFor(String parameterName, DataType dataType) {
		return EMSLUtil.castToBuiltInType(dataType)//
				.map(dt -> dt.equals(BuiltInDataTypes.EDOUBLE))//
				.orElse(false);
	}

	@Override
	public Double generateValueFor(String parameterName) {
		return rand.nextDouble();
	}
}
