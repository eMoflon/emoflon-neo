package org.emoflon.neo.neo4j.adapter;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.api.rules.IRule;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Value;

public class NeoMatch implements IMatch {

	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	Driver driver;

	private UUID uuid;
	private String patternName;
	private NeoPattern pattern;

	public NeoMatch(String name, NeoPattern pattern, String uuid, Driver driver) {
		this.driver = driver;
		this.patternName = name;
		this.pattern = pattern;
		//this.uuid = (UUID) uuid;// TODO Auto-generated constructor stub

	}

	@Override
	public IRule getRule() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isStillValid() {
		// TODO
		String statement = CypherPatternBuilder.createIsValidQuery(uuid);
		StatementResult result = driver.session().run(statement);
		return result.next().get("valid").asBoolean();
	}

	@Override
	public void destroy() {
		String statement = CypherPatternBuilder.createDestroyQuery(uuid);
		logger.info(statement);
		driver.session().run(statement);
	}

}
