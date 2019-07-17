package org.emoflon.neo.neo4j.adapter;

public abstract class NeoRuleAccess<Data extends NeoData, Mask extends NeoMask> {
	public abstract NeoRule rule();

	public abstract NeoRule rule(Mask mask);

	public abstract Mask mask();

	public abstract Data data(NeoMatch m);
}
