package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.eclipse.emf.common.util.EList;
import org.emoflon.neo.emsl.eMSL.NodeBlock;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.eMSL.RelationStatement;
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
	
	private Collection<NeoNode> nodes;
	private Collection<String> variables;
	
	// TODO: @jannik add properties of nodes and relationships
	// TODO: @jannik add parameter of relationships
	public NeoPattern(Pattern p, NeoCoreBuilder builder) {
		nodes = new ArrayList<>();
		this.builder = builder;
		this.p = p;
		
		for (NodeBlock n: p.getNodeBlocks()) {
						
			NeoNode tempN = new NeoNode(n.getType().getName(),n.getName());
			logger.info("(" + n.getName() + ":" + n.getType().getName() + ")");
			for(RelationStatement r:n.getRelationStatements()) {	
				tempN.addRelation(r.getName(), "", r.getValue().getType().getName(), r.getValue().getName());
				logger.info("        -[:" + r.getName() + "]->(" + r.getValue().getName() + ":" + r.getValue().getType().getName() + ")");
			}
			nodes.add(tempN);
			
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
		
		String cypherQuery = CypherPatternBuilder.createCypherQuery(nodes);
		logger.info(cypherQuery);
		
		StatementResult result = driver.session().run(cypherQuery);
		Collection<IMatch> matches = new ArrayList<>();
		
		while(result.hasNext()) {
			Record res = result.next();
			matches.add(new NeoMatch(getName(), p, res.values()));
			logger.info(res.values().toString());
		}
		return matches;
		
	}
	
}
