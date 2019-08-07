package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.emoflon.neo.neo4j.adapter.patterns.NeoPattern;

/**
 * Class created, when a pattern has a condition. Runs relevant pattern and
 * constraint matching checks
 * 
 * @author Jannik Hinz
 *
 */
public class NeoCondition {

	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	private Optional<NeoCoreBuilder> builder;
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
		this(c, p, name, Optional.of(builder), helper);
	}

	public NeoCondition(NeoConstraint c, NeoPattern p, String name, Optional<NeoCoreBuilder> builder,
			NeoHelper helper) {
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
		return determineMatches(0);
	}

	public String getQuery() {
		var condData = c.getConditionData();
		return CypherPatternBuilder.conditionQuery_copyPaste(p.getNodes(), condData.getOptionalMatchString(),
				condData.getWhereClause(), helper.getNodes(), p.isNegated(), 0);
	}

	/**
	 * Get the data and nodes from the (nested) conditions and runs the query in the
	 * database, analyze the results and return the matches
	 * 
	 * @param limit number of matches, that should be returned - 0 if infinite
	 * @return Collection<IMatch> return a list of all Matches of the pattern with
	 *         condition matching
	 */
	public Collection<NeoMatch> determineMatches(int limit) {

		var bld = builder.orElseThrow();

		logger.info("Searching matches for Pattern: " + p.getName() + " WHEN " + c.getName());

		// collecting the data
		var condData = c.getConditionData();

		// creating the query string
		var cypherQuery = CypherPatternBuilder.conditionQuery(p.getNodes(), condData.getOptionalMatchString(),
				condData.getWhereClause(), helper.getNodes(), p.isNegated(), limit);
		logger.debug(cypherQuery);

		// run the query
		var result = bld.executeQuery(cypherQuery);

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
	 * @param limit number of matches, that should be returned - 0 if infinite
	 * @return Collection<IMatch> return a list of all Matches of the pattern with
	 *         condition matching
	 */
	public boolean isStillValid(NeoMatch m) {

		var bld = builder.orElseThrow();

		logger.info("Check if match for " + p.getName() + " WHEN " + c.getName() + " is still valid");

		// collecting the data
		var condData = c.getConditionData();

		// creating the query string
		var cypherQuery = CypherPatternBuilder.conditionQuery_isStillValid(p.getNodes(), condData.getOptionalMatchString(),
				condData.getWhereClause(), helper.getNodes(), p.isNegated(), m);
		logger.debug(cypherQuery);

		// run the query
		var result = bld.executeQuery(cypherQuery);

		// analyze and return results
		var matches = new ArrayList<NeoMatch>();
		while (result.hasNext()) {
			var record = result.next();
			matches.add(new NeoMatch(p, record));
		}

		return matches.size() == 1;
	}
	
}
