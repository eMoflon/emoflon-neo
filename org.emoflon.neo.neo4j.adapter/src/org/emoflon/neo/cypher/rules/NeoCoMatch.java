package org.emoflon.neo.cypher.rules;

import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.patterns.NeoPattern;
import org.emoflon.neo.engine.api.rules.ICoMatch;
import org.neo4j.driver.v1.Record;

public class NeoCoMatch extends NeoMatch implements ICoMatch {
	private String matchHash;

	public NeoCoMatch(NeoPattern copattern, Record record) {
		super(copattern, record);
		this.matchHash = record.get(NeoMatch.getIdParameter()).toString();
	}

	public String getMatchHashCode() {
		return matchHash;
	}
}
