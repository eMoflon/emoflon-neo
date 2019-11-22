package org.emoflon.neo.cypher.rules;

import java.util.Collection;
import java.util.stream.Stream;

import org.emoflon.neo.cypher.common.NeoMask;
import org.emoflon.neo.cypher.patterns.NeoData;

public abstract class NeoRuleCoAccess<Data extends NeoData, CoData extends NeoData, Mask extends NeoMask> extends NeoRuleAccess<Data, Mask> {
	
	public abstract Stream<CoData> codata(Collection<NeoCoMatch> m);
}
