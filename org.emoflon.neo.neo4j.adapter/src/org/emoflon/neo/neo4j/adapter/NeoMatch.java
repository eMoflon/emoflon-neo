package org.emoflon.neo.neo4j.adapter;

import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.api.rules.IRule;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;

public class NeoMatch implements IMatch {

	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	Driver driver;

	private Record rec;
	private Collection<NeoNode>  nodes;
	private String pName;
	private Map<String, Object> recMap;

	public NeoMatch(String pName, Collection<NeoNode> nodes, Record rec, Driver driver) {
		this.driver = driver;
		this.pName = pName;
		this.nodes = nodes;
		this.rec = rec;
		
		recMap = rec.asMap();
		
		String temp = "[";
		for(NeoNode n:nodes) {
			if(rec.asMap().containsKey(n.getVarName())) {
				n.setId(recMap.get(n.getVarName()));
				temp += n.getVarName()+":"+n.getClassType()+"<"+n.getId()+">, ";
			}
		}
		temp += "]";
		logger.info(temp);
	}

	@Override
	public IRule getRule() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isStillValid() {
		logger.info("Check if pattern " + pName + " is still valid");
		String cypherQuery = CypherPatternBuilder.readQuery(nodes,false,true);
		logger.info(cypherQuery);
		StatementResult result = driver.session().run(cypherQuery);
		return result.hasNext();
	}

	@Override
	public void destroy() {
		String statement = null;
		logger.info(statement);
		driver.session().run(statement);
	}

}
