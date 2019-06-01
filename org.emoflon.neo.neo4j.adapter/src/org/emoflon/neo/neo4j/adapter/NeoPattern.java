package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.ConditionOperator;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.api.rules.IPattern;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;

public class NeoPattern implements IPattern {

	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	Driver driver;
	private Pattern p;
	
	private boolean injective;

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

		this.injective = true;
		
		generateNodesAndRelations();

	}
	
	private void generateNodesAndRelations() {

		nodes.clear();
		conditions.clear();

		for (var n : p.getBody().getNodeBlocks()) {

			NeoNode node = new NeoNode(n.getType().getName(), n.getName());
			
			n.getProperties().forEach(p -> node.addProperty(
					p.getType().getName(), 
					NeoUtil.handleValue(p.getValue())
			));

			// Get all relationships
			n.getRelations().forEach(r -> node.addRelation(
					new NeoRelation(
							r.getType().getName(),
							r.getProperties(), 
							r.getTarget().getType().getName(), 
							r.getTarget().getName())
					)
			);
			
			//		r.getTarget().getType().getName(), r.getTarget().getName())));

			// Get all properties or conditions
			/*n.getProperties().forEach(p -> {
				if (p.getOp().equals(ConditionOperator.EQ))
					node.addProperty(p.getType().getName(), NeoUtil.handleValue(p.getValue()));
				else
					conditions.add(new NeoCondition(p.getType().getName(), p.getOp(), NeoUtil.handleValue(p.getValue()),
							node.getVarName()));
			});*/

			nodes.add(node);
		}

	}

	@Override
	public Collection<IMatch> getValidMatches(String uuid) {

		generateNodesAndRelations();

		NeoNode matchnode = new NeoNode("Match", "matchingNode");
		matchnode.addProperty("uuid", uuid);

		/*for (NeoNode node : nodes) {
			relations.add(
			//new NeoRelation("matches_" + node.getVarName(), matchnode, node.getClassType(), node.getVarName()));
		}*/

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
		return p.getBody().getName();
	}

	@Override
	public Collection<IMatch> getMatches() {

		logger.info("Searching matches for Pattern: " + getName());

		String cypherQuery = CypherPatternBuilder.readQuery(nodes,injective,false);
		logger.info(cypherQuery);

		StatementResult result = driver.session().run(cypherQuery);
		matches = new ArrayList<>();

		while (result.hasNext()) {
			Record res = result.next();
			matches.add(new NeoMatch(getName(), nodes, res, driver));
		}
		if (matches.isEmpty()) {
			logger.error("NO MATCHES FOUND!");
		}
		return matches;

	}

	@Override
	public void setInjectivity(Boolean injective) {
		this.injective = injective;
	}
	
	// TODO[Jannik]: implements this
	public void isStillValidForAllMatches() {
		
	}
}
