package org.emoflon.neo.neo4j.adapter.patterns;

import java.util.Collection;
import java.util.stream.Stream;

public abstract class NeoPatternAccess<Data extends NeoData, Mask extends NeoMask> {
	public abstract NeoPattern matcher();

	public abstract NeoPattern matcher(Mask mask);

	public abstract Mask mask();

	public abstract Stream<Data> data(Collection<NeoMatch> m);
}
