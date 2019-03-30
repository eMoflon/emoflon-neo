package org.emoflon.neo.neo4j.adapter;

import java.util.Collection;

import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.api.rules.IPattern;

public class NeoPattern implements IPattern {

	public NeoPattern(Pattern p, NeoCoreBuilder builder) {
		// TODO [Jannik] extract all necessary infos
	}

	@Override
	public String getName() {
		// TODO [Jannik]
		return null;
	}

	@Override
	public Collection<IMatch> getMatches() {
		// TODO [Jannik] Execute cypher query and return matches (containing ids)?
		return null;
	}

}
