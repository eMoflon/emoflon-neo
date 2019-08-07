package org.emoflon.neo.neo4j.adapter.patterns;

import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.neo4j.adapter.NeoCoreBuilder;
import org.emoflon.neo.neo4j.adapter.NeoMask;

public class NeoPatternFactory {

	public static NeoPattern createNeoPattern(Pattern pattern) {
		return new NeoPatternOnlyQuery(pattern);
	}

	public static NeoPattern createNeoPattern(Pattern pattern, NeoCoreBuilder builder) {
		return new NeoPatternQueryAndMatch(pattern, builder);
	}

	public static NeoPattern createNeoPattern(Pattern pattern, NeoCoreBuilder builder, NeoMask mask) {
		return new NeoPatternQueryAndMatch(pattern, builder, mask);
	}
}
