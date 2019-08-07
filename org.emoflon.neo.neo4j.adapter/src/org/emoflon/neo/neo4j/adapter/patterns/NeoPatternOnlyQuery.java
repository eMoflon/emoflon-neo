package org.emoflon.neo.neo4j.adapter.patterns;

import java.util.Collection;

import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.neo4j.adapter.NeoMatch;
import org.neo4j.driver.v1.Record;

public class NeoPatternOnlyQuery extends NeoPattern {

	NeoPatternOnlyQuery(Pattern p) {
		super(p);
	}

	@Override
	public Collection<NeoMatch> determineMatches() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<NeoMatch> determineMatches(int limit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Record getData(NeoMatch m) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isStillValid(NeoMatch neoMatch) {
		throw new UnsupportedOperationException();
	}
}
