package org.emoflon.neo.engine.modules.valueGenerators;

import java.util.List;
import java.util.Random;

import org.emoflon.neo.emsl.eMSL.DataType;
import org.emoflon.neo.emsl.eMSL.EnumLiteral;
import org.emoflon.neo.emsl.eMSL.UserDefinedType;
import org.emoflon.neo.engine.generator.modules.IParameterValueGenerator;

public class RandomEnumLiteralGenerator implements IParameterValueGenerator<DataType, EnumLiteral> {

	private Random rand;

	public RandomEnumLiteralGenerator() {
		this(new Random());
	}

	public RandomEnumLiteralGenerator(long seed) {
		this(new Random(seed));
	}

	public RandomEnumLiteralGenerator(Random randomGenerator) {
		rand = randomGenerator;
	}

	@Override
	public boolean generatesValueFor(String parameterName, DataType dataType) {
		return dataType instanceof UserDefinedType;
	}

	@Override
	public EnumLiteral generateValueFor(String parameterName, DataType dataType) {
		if (dataType instanceof UserDefinedType) {
			UserDefinedType type = (UserDefinedType) dataType;
			List<EnumLiteral> literals = type.getReference().getLiterals();
			return literals.get(rand.nextInt(literals.size()));
		} else
			throw new UnsupportedOperationException(
					"This generator does not support the specified data-type: " + dataType);
	}
}
