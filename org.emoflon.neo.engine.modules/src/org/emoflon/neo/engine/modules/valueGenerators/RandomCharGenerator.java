package org.emoflon.neo.engine.modules.valueGenerators;

import java.util.Random;

import org.emoflon.neo.emsl.eMSL.BuiltInDataTypes;
import org.emoflon.neo.emsl.eMSL.DataType;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.engine.generator.modules.IParameterValueGenerator;

/**
 * Generates a random letter from the English alphabet (possibly capitalized).
 */
public class RandomCharGenerator implements IParameterValueGenerator<DataType, Character> {

	private final String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private Random rand;

	public RandomCharGenerator() {
		this(new Random());
	}

	public RandomCharGenerator(long seed) {
		this(new Random(seed));
	}

	public RandomCharGenerator(Random randomGenerator) {
		rand = randomGenerator;
	}

	@Override
	public boolean generatesValueFor(String parameterName, DataType dataType) {
		return EMSLUtil.castToBuiltInType(dataType)//
				.map(dt -> dt.equals(BuiltInDataTypes.ECHAR))//
				.orElse(false);
	}

	@Override
	public Character generateValueFor(String parameterName) {
		return alphabet.charAt(rand.nextInt(alphabet.length()));
	}
}
