package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.NodeBlock;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.eMSL.PropertyStatement;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.api.rules.IPattern;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;

public class NeoPattern implements IPattern {

	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	Driver driver;
	private Pattern p;

	private Collection<NeoNode> nodes;
	private Collection<NeoRelation> relations;
	private Collection<NeoCondition> conditions;
	private Collection<IMatch> matches;

	public NeoPattern(Pattern p, NeoCoreBuilder builder) {
		nodes = new ArrayList<>();
		relations = new ArrayList<>();
		conditions = new ArrayList<>();
		this.driver = builder.getDriver();
		this.p = p;

		generateNodesAndRelations();

	}
	
	private void generateNodesAndRelations() {
		
		nodes.clear();
		relations.clear();
		conditions.clear();
		
		for (NodeBlock n : p.getNodeBlocks()) {

			NeoNode node = new NeoNode(n.getType().getName(), n.getName());

			// Get all relationships
			n.getRelationStatements().forEach(r -> relations.add(new NeoRelation(r.getName(), r.getPropertyStatements(),
					node, r.getValue().getType().getName(), r.getValue().getName())));

			// Get all properties or conditions
			n.getConditionStatements().forEach(c -> {
				if (c.getOp().toString() == "==")
					node.addProperty(c.getName(), c.getValue());
				else
					conditions.add(new NeoCondition(c.getName(), c.getOp(), c.getValue(), node.getVarName()));
			});

			nodes.add(node);
		}
		
	}
	
	
	@Override
	public Collection<IMatch> getValidMatches(String uuid) {
		
		generateNodesAndRelations();
		
		NeoNode matchnode = new NeoNode("Match", "matchingNode");
		matchnode.addProperty("uuid", uuid);
		
		for(NeoNode node :nodes) {
			relations.add(new NeoRelation("matches", new ArrayList<PropertyStatement>(), matchnode, node.getClassType(), node.getVarName()));
		}
		
		nodes.add(matchnode);

		logger.info("Searching matches for Pattern: " + getName());

		String cypherQuery = CypherPatternBuilder.createCypherValidQuery(nodes, conditions, relations, getName());
		logger.info(cypherQuery);

		StatementResult result = driver.session().run(cypherQuery);
		matches = new ArrayList<>();

		while (result.hasNext()) {
			Record res = result.next();
			matches.add(new NeoMatchValid());
			logger.info(res.get("uuid").toString());
		}
		if (matches.isEmpty()) {
			logger.error("NO MATCHES FOUND!");
		}
		return matches;

	}

	@Override
	public String getName() {
		return p.getName();
	}

	@Override
	public Collection<IMatch> getMatches() {

		logger.info("Searching matches for Pattern: " + getName());

		String cypherQuery = CypherPatternBuilder.createCypherQuery(nodes, conditions, relations, getName());
		logger.info(cypherQuery);

		StatementResult result = driver.session().run(cypherQuery);
		matches = new ArrayList<>();

		while (result.hasNext()) {
			Record res = result.next();
			matches.add(new NeoMatch(getName(), this, res.get("uuid").toString(), driver));
			logger.info(res.get("uuid").toString());
		}
		if (matches.isEmpty()) {
			logger.error("NO MATCHES FOUND!");
		}
		return matches;

	}

}
