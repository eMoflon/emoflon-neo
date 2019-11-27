package org.emoflon.neo.engine.modules.valueGenerators;

import java.util.Random;

import org.emoflon.neo.emsl.eMSL.BuiltInDataTypes;
import org.emoflon.neo.emsl.eMSL.DataType;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.engine.generator.modules.IParameterValueGenerator;

public class RandomBooleanGenerator implements IParameterValueGenerator<DataType, Boolean> {

	private Random rand;

	public RandomBooleanGenerator() {
		this(new Random());
	}

	public RandomBooleanGenerator(long seed) {
		this(new Random(seed));
	}

	public RandomBooleanGenerator(Random randomGenerator) {
		rand = randomGenerator;
	}

	@Override
	public boolean generatesValueFor(String parameterName, DataType dataType) {
		return EMSLUtil.castToBuiltInType(dataType)//
				.map(dt -> dt.equals(BuiltInDataTypes.EBOOLEAN))//
				.orElse(false);
	}

	@Override
	public Boolean generateValueFor(String parameterName, DataType dataType) {
		return rand.nextBoolean();
	}
}
