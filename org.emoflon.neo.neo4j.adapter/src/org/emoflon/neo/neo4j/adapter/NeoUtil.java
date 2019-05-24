package org.emoflon.neo.neo4j.adapter;

import org.emoflon.neo.emsl.eMSL.EnumValue;
import org.emoflon.neo.emsl.eMSL.PrimitiveBoolean;
import org.emoflon.neo.emsl.eMSL.PrimitiveInt;
import org.emoflon.neo.emsl.eMSL.PrimitiveString;
import org.emoflon.neo.emsl.eMSL.Value;

public class NeoUtil {

	public static String handleValue(Value value) {
		if (value instanceof PrimitiveString)
			return PrimitiveString.class.cast(value).getLiteral();

		// TODO[Jannik] Is this the best way of handling ints, bools, and enum literals?
		if (value instanceof PrimitiveInt)
			return Integer.toString(PrimitiveInt.class.cast(value).getLiteral());

		if (value instanceof PrimitiveBoolean)
			return Boolean.toString(PrimitiveBoolean.class.cast(value).isTrue());

		if (value instanceof EnumValue)
			return EnumValue.class.cast(value).getLiteral().getName();

		// TODO[Jannik] How to handle attribute expressions?

		throw new IllegalArgumentException("Not yet able to handle: " + value);
	}

}
