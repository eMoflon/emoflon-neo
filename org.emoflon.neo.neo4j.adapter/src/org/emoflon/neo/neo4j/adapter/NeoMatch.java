package org.emoflon.neo.neo4j.adapter;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.Pattern;
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
	private Pattern pattern;
	private Collection<Value> nodes;

	public NeoMatch(String name, Pattern pattern, List<Value> nodes, UUID uuid, Driver driver) {
		this.driver = driver;
		
		this.uuid = uuid;
		this.patternName = name;
		this.pattern = pattern;
		this.nodes = nodes;// TODO Auto-generated constructor stub
		
		String nodeID = nodes.get(0).toString();
		nodeID = nodeID.split("<")[1].split(">")[0];
		
		String statement = "MATCH (n:Match) WHERE id(n)=" + nodeID + " SET n.uuid = \"" + uuid + "\", n.valid = true, n.pattern = \"" + patternName + "\"";
		logger.info(statement);
		driver.session().run(statement);

	}


	@Override
	public IRule getRule() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isStillValid() {
		String statement = "MATCH (n:Match {uuid: \""+ uuid +"\"}) RETURN n.valid AS valid";
		StatementResult result = driver.session().run(statement);
		
		return result.next().get("valid").asBoolean();
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}

}
