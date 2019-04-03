package org.emoflon.neo.neo4j.adapter;

import java.util.List;
import java.util.UUID;

import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.api.rules.IRule;
import org.neo4j.driver.v1.Value;

public class NeoMatch implements IMatch {

	private UUID id;
	private String patternName;
	private Pattern p;
	private List<Value> result;

	public NeoMatch(String name, Pattern p2, List<Value> values) {
		this.id = UUID.randomUUID();
		this.patternName = name;
		this.p = p2;
		this.result = values;// TODO Auto-generated constructor stub
	}

	@Override
	public IRule getRule() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isStillValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}

}
