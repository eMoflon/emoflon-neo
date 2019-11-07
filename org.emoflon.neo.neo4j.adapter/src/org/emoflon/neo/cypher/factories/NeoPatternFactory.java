package org.emoflon.neo.cypher.factories;

import org.emoflon.neo.cypher.models.EmptyBuilder;
import org.emoflon.neo.cypher.models.IBuilder;
import org.emoflon.neo.cypher.patterns.NeoPattern;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.refinement.EMSLFlattener;
import org.emoflon.neo.emsl.util.FlattenerException;

public class NeoPatternFactory {

	public static NeoPattern createNeoPattern(Pattern pattern) {
		return createNeoPattern(pattern, new EmptyBuilder());
	}

	public static NeoPattern createNeoPattern(Pattern pattern, IBuilder builder) {
		try {
			var flatPattern = EMSLFlattener.flattenPattern(pattern);
			return new NeoPattern(flatPattern, builder, false);
		} catch (FlattenerException e) {
			e.printStackTrace();
			return null;
		}
	}
}