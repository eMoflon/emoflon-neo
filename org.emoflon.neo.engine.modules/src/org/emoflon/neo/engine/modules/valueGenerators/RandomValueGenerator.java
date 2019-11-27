package org.emoflon.neo.engine.modules.valueGenerators;

import java.util.Random;

import org.emoflon.neo.emsl.eMSL.DataType;
import org.emoflon.neo.emsl.eMSL.UserDefinedType;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.engine.generator.modules.IParameterValueGenerator;

/**
 * Composite generator, generates random values for all DataTypes using the
 * type-specific random value generators.
 */
public class RandomValueGenerator implements IParameterValueGenerator<DataType, Object> {

	private Random rand;
	private RandomBooleanGenerator booleanGenerator;
	private RandomCharGenerator charGenerator;
	private RandomDoubleGenerator doubleGenerator;
	private RandomEnumLiteralGenerator enumGenerator;
	private RandomFloatGenerator floatGenerator;
	private RandomIntegerGenerator integerGenerator;
	private RandomLongGenerator longGenerator;
	private LoremIpsumStringValueGenerator stringGenerator;

	public RandomValueGenerator() {
		this(new Random());
	}

	public RandomValueGenerator(long seed) {
		this(new Random(seed));
	}

	public RandomValueGenerator(Random randomGenerator) {
		rand = randomGenerator;
		booleanGenerator = new RandomBooleanGenerator(rand);
		charGenerator = new RandomCharGenerator(rand);
		doubleGenerator = new RandomDoubleGenerator(rand);
		enumGenerator = new RandomEnumLiteralGenerator(rand);
		floatGenerator = new RandomFloatGenerator(rand);
		integerGenerator = new RandomIntegerGenerator(rand);
		longGenerator = new RandomLongGenerator(rand);
		stringGenerator = new LoremIpsumStringValueGenerator();
	}

	@Override
	public boolean generatesValueFor(String parameterName, DataType dataType) {
		return true;
	}

	@Override
	public Object generateValueFor(String parameterName, DataType dataType) {
		if (dataType instanceof UserDefinedType)
			return enumGenerator.generateValueFor(parameterName, dataType);
		else {
			var builtInType = EMSLUtil.castToBuiltInType(dataType);
			if (builtInType.isEmpty())
				throw new UnsupportedOperationException(
						"This generator does not support the specified data-type: " + dataType);
			switch (builtInType.get()) {
			case EBOOLEAN:
				return booleanGenerator.generateValueFor(parameterName, dataType);
			case ECHAR:
				return charGenerator.generateValueFor(parameterName, dataType);
			case EDOUBLE:
				return doubleGenerator.generateValueFor(parameterName, dataType);
			case EFLOAT:
				return floatGenerator.generateValueFor(parameterName, dataType);
			case EINT:
				return integerGenerator.generateValueFor(parameterName, dataType);
			case ELONG:
				return longGenerator.generateValueFor(parameterName, dataType);
			case ESTRING:
				return stringGenerator.generateValueFor(parameterName, dataType);
			default:
				throw new UnsupportedOperationException(
						"This generator does not support the specified data-type: " + dataType);
			}
		}
	}
}
