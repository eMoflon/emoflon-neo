package org.emoflon.neo.neo4j.adapter.rules;

import org.emoflon.neo.neo4j.adapter.patterns.NeoData;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMask;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;

public abstract class NeoRuleAccess<Data extends NeoData, Mask extends NeoMask> {
	public abstract NeoRule rule();

	public abstract NeoRule rule(Mask mask);

	public abstract Mask mask();

	public abstract Data data(NeoMatch m);
}