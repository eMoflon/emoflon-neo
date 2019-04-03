package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;

import org.emoflon.neo.emsl.eMSL.ConditionStatement;
import org.emoflon.neo.emsl.eMSL.NodeBlock;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.eMSL.PropertyStatement;
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
	private Collection<NeoRelation> relations;
	private Collection<NeoCondition> conditions;
	
	// TODO: @jannik add properties of nodes and relationships
	// TODO: @jannik add parameter of relationships
	public NeoPattern(Pattern p, NeoCoreBuilder builder) {
		nodes = new ArrayList<>();
		relations = new ArrayList<>();
		conditions = new ArrayList<>();
		this.builder = builder;
		this.p = p;
		
		for (NodeBlock n: p.getNodeBlocks()) {
						
			NeoNode node = new NeoNode(n.getType().getName(), n.getName());
			
			// Get all relationships
			n.getRelationStatements().forEach(r -> 
					relations.add(new NeoRelation(r.getName(), r.getPropertyStatements(), node, r.getValue().getType().getName(), r.getValue().getName())));
			
			/*for(RelationStatement r:n.getRelationStatements()) {
				NeoRelation rel = new NeoRelation(r.getName(), r.getPropertyStatements(), node, r.getValue().getType().getName(), r.getValue().getName());				
				relations.add(rel);
			}*/
			
			// Get all properties or conditions
			for(ConditionStatement c:n.getConditionStatements()) {
				
				if(c.getOp().toString() == "==") {
					node.addProperty(c.getName(), c.getValue());
					
				} else {
					NeoCondition cond = new NeoCondition(c.getName(), c.getOp(), c.getValue(), node.getVarName());
					conditions.add(cond);
				}
			}
			nodes.add(node);
			
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
		
		String cypherQuery = CypherPatternBuilder.createCypherQuery(nodes,conditions,relations);
		logger.info(cypherQuery);
		
		StatementResult result = driver.session().run(cypherQuery);
		Collection<IMatch> matches = new ArrayList<>();
		
		while(result.hasNext()) {
			Record res = result.next();
			matches.add(new NeoMatch(getName(), p, res.values()));
			logger.info(res.values().toString());
		}
		if(matches.isEmpty()) {
			logger.info("NO MATCHES!");
		}
		return matches;
		
	}
	
}
