package org.emoflon.neo.neo4j.adapter;

import org.apache.log4j.Logger;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.api.rules.IRule;
import org.neo4j.driver.v1.Driver;

public class NeoMatch implements IMatch {

	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	Driver driver;

	private String uuid;
	private String patternName;
	private NeoPattern pattern;

	public NeoMatch(String name, NeoPattern pattern, String uuid, Driver driver) {
		this.driver = driver;
		this.patternName = name;
		this.pattern = pattern;
		this.uuid = uuid;

	}

	@Override
	public IRule getRule() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isStillValid() {
		logger.info("Check if pattern" + patternName + "is still valid");
		return(pattern.getValidMatches(uuid).size() == 1);
	}

	@Override
	public void destroy() {
		String statement = CypherPatternBuilder.createDestroyQuery(uuid);
		logger.info(statement);
		driver.session().run(statement);
	}

}
