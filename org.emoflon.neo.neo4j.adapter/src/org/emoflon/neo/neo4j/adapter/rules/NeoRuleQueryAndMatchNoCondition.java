package org.emoflon.neo.neo4j.adapter.rules;

import java.util.Optional;

import org.emoflon.neo.emsl.eMSL.Rule;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMask;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.patterns.NeoPattern;
import org.emoflon.neo.neo4j.adapter.util.NeoQueryData;

public class NeoRuleQueryAndMatchNoCondition extends NeoRule {

	public NeoRuleQueryAndMatchNoCondition(Rule r, NeoPattern contextPattern, IBuilder builder, NeoMask mask,
			NeoQueryData neoQuery) {
		super(r, contextPattern, builder, mask, neoQuery);
	}

	@Override
	public Optional<NeoCoMatch> apply(NeoMatch match) {
		//TODO[Jannik]
		return Optional.empty();
	}

}
