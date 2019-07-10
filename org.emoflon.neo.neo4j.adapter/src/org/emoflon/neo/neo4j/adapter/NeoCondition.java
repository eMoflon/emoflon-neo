package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

/**
 * Class created, when a pattern has a condition. Runs relevant pattern and
 * constraint matching checks
 * 
 * @author Jannik Hinz
 *
 */
public class NeoCondition {

	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	private NeoCoreBuilder builder;
	private NeoHelper helper;
	private NeoConstraint c;
	private NeoPattern p;

	/**
	 * @param c       NeoConstraing the extracted constraint in the pattern p
	 * @param p       NeoPattern the pattern with the constraint c
	 * @param name    of the pattern
	 * @param builder for creating and running Cypher queries
	 * @param helper  for creating nodes and relation with a unique name and central
	 *                node storage
	 */
	public NeoCondition(NeoConstraint c, NeoPattern p, String name, NeoCoreBuilder builder, NeoHelper helper) {
		this.builder = builder;
		this.helper = helper;
		this.c = c;
		this.p = p;
	}

	/**
	 * Get the data and nodes from the (nested) conditions and runs the query in the
	 * database, analyze the results and return the matches
	 * 
	 * @return Collection<IMatch> return a list of all Matches of the pattern with
	 *         condition matching
	 */
	public Collection<NeoMatch> determineMatches() {

		logger.info("Searching matches for Pattern: " + p.getName() + " WHEN " + c.getName());

		// collecting the data
		var condData = c.getConditionData();

		// creating the query string
		var cypherQuery = CypherPatternBuilder.matchQuery(p.getNodes()) + CypherPatternBuilder.withQuery(p.getNodes())
				+ condData.getOptionalMatchString() + CypherPatternBuilder.constraint_withQuery(helper.getNodes());

		if (p.isNegated())
			cypherQuery += "\nWHERE NOT(" + condData.getWhereClause() + ")";
		else
			cypherQuery += "\nWHERE " + condData.getWhereClause();

		cypherQuery += "\n" + CypherPatternBuilder.returnQuery(p.getNodes());

		logger.debug(cypherQuery);

		// run the query
		var result = builder.executeQuery(cypherQuery);

		// analyze and return results
		var matches = new ArrayList<NeoMatch>();
		while (result.hasNext()) {
			var record = result.next();
			matches.add(new NeoMatch(p, record));
		}

		return matches;
	}
	
	/**
	 * Get the data and nodes from the (nested) conditions and runs the query in the
	 * database, analyze the results and return the matches
	 * 
	 * @return Collection<IMatch> return a list of all Matches of the pattern with
	 *         condition matching
	 */
	public Collection<NeoMatch> determineMatches(int limit) {

		logger.info("Searching matches for Pattern: " + p.getName() + " WHEN " + c.getName());

		// collecting the data
		var condData = c.getConditionData();

		// creating the query string
		var cypherQuery = CypherPatternBuilder.matchQuery(p.getNodes()) + CypherPatternBuilder.withQuery(p.getNodes())
				+ condData.getOptionalMatchString() + CypherPatternBuilder.constraint_withQuery(helper.getNodes());

		if (p.isNegated())
			cypherQuery += "\nWHERE NOT(" + condData.getWhereClause() + ")";
		else
			cypherQuery += "\nWHERE " + condData.getWhereClause();

		cypherQuery += "\n" + CypherPatternBuilder.returnQuery(p.getNodes(),limit);

		logger.debug(cypherQuery);

		// run the query
		var result = builder.executeQuery(cypherQuery);

		// analyze and return results
		var matches = new ArrayList<NeoMatch>();
		while (result.hasNext()) {
			var record = result.next();
			matches.add(new NeoMatch(p, record));
		}

		return matches;
	}

}
