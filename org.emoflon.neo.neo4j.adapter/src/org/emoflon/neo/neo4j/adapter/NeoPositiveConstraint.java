package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.engine.api.constraints.IPositiveConstraint;
import org.emoflon.neo.engine.api.rules.IMatch;

/**
 * Class representing an ENFORCE constraint, storing all relevant data, creates
 * and runs the query for checking the constraint
 * 
 * @author Jannik Hinz
 *
 */
public class NeoPositiveConstraint implements IPositiveConstraint {

	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	private IBuilder builder;
	private NeoHelper helper;

	private AtomicPattern ap;
	private String name;
	private List<NeoNode> nodes;

	private boolean injective;
	private int uuid;

	private NeoMask mask;

	/**
	 * 
	 * @param ap        AtomicPattern of the FORBID constraint
	 * @param injective boolean if the pattern should be matches injective or not
	 * @param builder   for creating and running Cypher queries
	 * @param helper    for creating nodes and
	 */
	public NeoPositiveConstraint(AtomicPattern ap, boolean injective, IBuilder builder, NeoHelper helper,
			NeoMask mask) {
		this.uuid = helper.addConstraint();
		this.builder = builder;
		this.helper = helper;
		this.name = ap.getName();
		this.injective = injective;
		this.mask = mask;

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
		return CypherPatternBuilder.constraint_matchQuery(nodes, injective, uuid, mask);
	}

	/**
	 * Return the corresponding OPTIONAL MATCH query with injectivity block and WITH
	 * clause (for use in conditions)
	 * 
	 * @return OPTIONAL MATCH xyz WHERE injective WITH nodes (query part for this
	 *         constraint)
	 */
	public String getQueryString_MatchCondition() {
		return CypherPatternBuilder.condition_matchQuery(nodes, injective, mask);
	}

	/**
	 * Return the corresponding WHERE query (for use in constraints)
	 * 
	 * @return WHERE count(xyz) > 0 (query part for this constraint)
	 */
	public String getQueryString_WhereConstraint() {
		return CypherPatternBuilder.wherePositiveConstraintQuery(uuid);
	}

	/**
	 * Return the corresponding WHERE query (for use in conditions)
	 * 
	 * @return WHERE xy IS NOT NULL AND yz IS NOT NULL (query part for this
	 *         constraint)
	 */
	public String getQueryString_WhereConditon() {
		return CypherPatternBuilder.wherePositiveConditionQuery(nodes);
	}

	/**
	 * Runs the Matching Query for positive constraint and checks if the constraints
	 * is satisfied
	 * 
	 * @return true if the pattern matcher find any match in the clause and else
	 *         false
	 */
	@Override
	public boolean isSatisfied() {

		if (getMatch() != null)
			return true;
		else
			return false;

	}

	/**
	 * Creates and runs the Query in the database for checking the positive
	 * constraint violations
	 * 
	 * @return NeoMatches return a list of found Matches of the constraint
	 */
	@Override
	public IMatch getMatch() {
		logger.info("Check constraint: ENFORCE " + ap.getName());

		var cypherQuery = CypherPatternBuilder.readQuery(nodes, injective, mask);
		logger.debug(cypherQuery);

		var result = builder.executeQuery(cypherQuery);

		if (result.hasNext()) {
			logger.info("Found match(es). Constraint: ENFORCE " + ap.getName() + " is complied!");
			return new NeoMatch(null, result.next());
		}
		logger.info("Not matches found. Constraint: ENFORCE " + ap.getName() + " is NOT complied!");
		return null;
	}

}
