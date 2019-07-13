package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.engine.api.constraints.INegativeConstraint;
import org.emoflon.neo.engine.api.rules.IMatch;

/**
 * Class representing an FORBID constraint, storing all relevant data, creates
 * and runs the query for checking the constraint
 * 
 * @author Jannik Hinz
 *
 */
public class NeoNegativeConstraint implements INegativeConstraint {

	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	private NeoCoreBuilder builder;
	private NeoHelper helper;

	private AtomicPattern ap;
	private String name;
	private List<NeoNode> nodes;

	private boolean injective;
	private int uuid;

	/**
	 * 
	 * @param ap        AtomicPattern of the FORBID constraint
	 * @param injective boolean if the pattern should be matches injective or not
	 * @param builder   for creating and running Cypher queries
	 * @param helper    for creating nodes and
	 */
	public NeoNegativeConstraint(AtomicPattern ap, boolean injective, NeoCoreBuilder builder, NeoHelper helper) {
		this.uuid = helper.addConstraint();
		this.builder = builder;
		this.helper = helper;
		this.name = ap.getName();
		this.injective = injective;
		
		this.ap = helper.getFlattenedPattern(ap);
		
		// Extracts all necessary information data from the Atomic Pattern
		this.nodes = new ArrayList<>();
		this.nodes = this.helper.extractNodesAndRelations(ap.getNodeBlocks());
	}

	/**
	 * Return the name of the constraint
	 * 
	 * @return name of the constraint
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the AtomicPattern of the constraint
	 * 
	 * @return AtomicPattern of the constraint
	 */
	public AtomicPattern getPattern() {
		return ap;
	}

	/**
	 * Returns a collection of the Nodes from the constraint
	 * 
	 * @return NeoNode collection of the Nodes from the constraint
	 */
	public Collection<NeoNode> getNodes() {
		return nodes;
	}

	/**
	 * Return the corresponding OPTIONAL MATCH query with injectivity block and WITH
	 * clause (for use in constraints)
	 * 
	 * @return OPTIONAL MATCH xyz WHERE injective WITH nodes (query part for this
	 *         constraint)
	 */
	public String getQueryString_MatchConstraint() {
		return CypherPatternBuilder.constraint_matchQuery(nodes, injective, uuid);
	}

	/**
	 * Return the corresponding OPTIONAL MATCH query with injectivity block and WITH
	 * clause (for use in conditions)
	 * 
	 * @return OPTIONAL MATCH xyz WHERE injective WITH nodes (query part for this
	 *         constraint)
	 */
	public String getQueryString_MatchCondition() {
		return CypherPatternBuilder.condition_matchQuery(nodes, injective);
	}

	/**
	 * Return the corresponding WHERE query (for use in constraints)
	 * 
	 * @return WHERE count(xyz) = 0 (query part for this constraint)
	 */
	public String getQueryString_WhereConstraint() {
		return CypherPatternBuilder.whereNegativeConstraintQuery(uuid);
	}

	/**
	 * Return the corresponding WHERE query (for use in conditions)
	 * 
	 * @return WHERE xy IS NULL OR yz IS NULL (query part for this constraint)
	 */
	public String getQueryString_WhereConditon() {
		return CypherPatternBuilder.whereNegativeConditionQuery(nodes);
	}

	/**
	 * Runs the Matching Query for negative constraint and checks if the constraints
	 * is satisfied
	 * 
	 * @return true if the pattern matcher not find any violation in the clause and
	 *         else false
	 */
	@Override
	public boolean isSatisfied() {

		if (getViolations() == null)
			return true;
		else
			return false;

	}

	/**
	 * Creates and runs the Query in the database for checking the negative
	 * constraint violations
	 * 
	 * @return NeoMatches return a list of violating Matches of the constraint
	 */
	@Override
	public Collection<IMatch> getViolations() {

		logger.info("Check constraint: FORBID " + ap.getName());

		// create query
		var cypherQuery = CypherPatternBuilder.readQuery(nodes, injective);
		logger.debug(cypherQuery);

		// execute query
		var result = builder.executeQuery(cypherQuery);

		// analyze and return results
		var matches = new ArrayList<IMatch>();
		while (result.hasNext()) {
			matches.add(new NeoMatch(null, result.next()));
		}

		if (!matches.isEmpty()) {
			logger.info("Found match(es). Constraint: FORBID " + ap.getName() + " is NOT complied!");
			return matches;
		} else {
			logger.info("Not matches found. Constraint: FORBID " + ap.getName() + " is complied!");
			return null;
		}

	}

}
