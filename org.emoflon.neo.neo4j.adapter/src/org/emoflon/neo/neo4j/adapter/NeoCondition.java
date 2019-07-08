package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.emoflon.neo.engine.api.rules.IMatch;

public class NeoCondition {

	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	private NeoCoreBuilder builder;
	private NeoHelper helper;
	private NeoConstraint c;
	private NeoPattern p;

	private Collection<String> nodesAndRefs;
	private Collection<NeoNode> nodesAndRefsN;

	public NeoCondition(NeoConstraint c, NeoPattern p, String name, NeoCoreBuilder builder, NeoHelper helper) {
		this.builder = builder;
		this.helper = helper;
		this.c = c;
		this.p = p;
		this.nodesAndRefs = new ArrayList<>();
		this.nodesAndRefsN = new ArrayList<>();
	}

	public Collection<IMatch> determineMatches() {

		logger.info("Searching matches for Pattern: " + p.getName() + " WHEN " + c.getName());

		var condData = c.getConditionData();
		
		removeDuplicates(p.getNodes());
		removeDuplicates(condData.getNodes());

		var cypherQuery = CypherPatternBuilder.matchQuery(p.getNodes());
		cypherQuery += condData.getOptionalMatchString();
		cypherQuery += CypherPatternBuilder.withConstraintQuery(helper.getNodes());
		if (p.isNegated())
			cypherQuery += "\nWHERE NOT(" + condData.getWhereClause() + ")";
		else
			cypherQuery += "\nWHERE " + condData.getWhereClause();
		cypherQuery += "\n" + CypherPatternBuilder.returnQuery(p.getNodes());

		logger.debug(cypherQuery);

		var result = builder.executeQuery(cypherQuery);

		var matches = new ArrayList<IMatch>();
		while (result.hasNext()) {
			var record = result.next();
			matches.add(new NeoMatch(p, record));
		}

		return matches;
	}

	private void removeDuplicates(Collection<NeoNode> nodes) {
		for (NeoNode node : nodes) {

			if (!nodesAndRefs.contains(node.getVarName())) {
				nodesAndRefs.add(node.getVarName());
				nodesAndRefsN.add(node);
			}

			for (NeoRelation rel : node.getRelations()) {
				if (!nodesAndRefs.contains(rel.getVarName())) {
					nodesAndRefs.add(rel.getVarName());
				}
			}
		}
	}

}
