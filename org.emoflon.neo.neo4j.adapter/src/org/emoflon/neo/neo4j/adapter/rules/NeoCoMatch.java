package org.emoflon.neo.neo4j.adapter.rules;

import org.emoflon.neo.engine.api.rules.ICoMatch;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.patterns.NeoPattern;
import org.neo4j.driver.v1.Record;

public class NeoCoMatch extends NeoMatch implements ICoMatch {

	String matchHash;
	
	public NeoCoMatch(NeoPattern pattern, Record record, String matchHash) {
		super(pattern, record);
		this.matchHash = matchHash;
	}
	
	public String getMatchHashCode() {
		return matchHash;
	}
	
}
