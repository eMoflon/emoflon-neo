package org.emoflon.neo.neo4j.adapter.rules;

import org.emoflon.neo.engine.api.rules.ICoMatch;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.patterns.NeoPattern;
import org.neo4j.driver.v1.Record;

public class NeoCoMatch extends NeoMatch implements ICoMatch {

	public NeoCoMatch(NeoRule rule, Record record) {
		super(rule, record);
	}

}
