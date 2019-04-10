package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.NodeBlock;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.api.rules.IPattern;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;

public class NeoPattern implements IPattern {

	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	private NeoCoreBuilder builder;
	private Pattern p;

	private Collection<NeoNode> nodes;
	private Collection<NeoRelation> relations;
	private Collection<NeoCondition> conditions;

	public NeoPattern(Pattern p, NeoCoreBuilder builder) {
		nodes = new ArrayList<>();
		relations = new ArrayList<>();
		conditions = new ArrayList<>();
		this.builder = builder;
		this.p = p;

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
	public String getName() {
		return p.getName();
	}

	@Override
	public Collection<IMatch> getMatches() {

		Driver driver = builder.getDriver();
		logger.info("Searching matches for Pattern: " + getName());

		String cypherQuery = CypherPatternBuilder.createCypherQuery(nodes, conditions, relations);
		logger.info(cypherQuery);

		StatementResult result = driver.session().run(cypherQuery);
		Collection<IMatch> matches = new ArrayList<>();

		while (result.hasNext()) {
			Record res = result.next();
			matches.add(new NeoMatch(getName(), p, res.values()));
			logger.info(res.values().toString());
		}
		if (matches.isEmpty()) {
			logger.error("NO MATCHES FOUND!");
		}
		return matches;

	}

}
