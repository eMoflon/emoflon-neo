package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.eclipse.emf.common.util.EList;
import org.emoflon.neo.emsl.eMSL.NodeBlock;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.eMSL.TypeReference;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.api.rules.IPattern;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.apache.log4j.Logger;

public class NeoPattern implements IPattern {
	
	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	private NeoCoreBuilder builder;
	private Pattern p;
	
	private Collection<String> nodes;
	
	public NeoPattern(Pattern p, NeoCoreBuilder builder) {
		nodes = new ArrayList<>();
		this.builder = builder;
		this.p = p;
		
		System.out.println(p.getNodeBlocks().size());
		
		for (NodeBlock n: p.getNodeBlocks()) {
			nodes.add(n.getName() + ":" + n.getType().getName());
			logger.info(n.getName() + ":" + n.getType().getName());
		}
		
	}

	@Override
	public String getName() {
		return p.getName();
	}

	@Override
	public Collection<IMatch> getMatches() {
		
		Driver driver = builder.getDriver();
		logger.info("Searching matches for Pattern: " + getName());
		
		String statement = "MATCH (sokoban:Sokoban) RETURN sokoban";
		logger.info(statement);
		
		StatementResult result = driver.session().run(statement);
		Collection<IMatch> matches = new ArrayList<>();
		
		while(result.hasNext()) {
			Record res = result.next();
			matches.add(new NeoMatch(getName(), p, res.values()));
			logger.info(res.values().toString());
		}
		return matches;
	}
	
	public String returnNodes() {
		
		String statement = "";
		for(String s: nodes) {
			statement += "(" + s + ")"; 
		}
		return statement;
	}
}
