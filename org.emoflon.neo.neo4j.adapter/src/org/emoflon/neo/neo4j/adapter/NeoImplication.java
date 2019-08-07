package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.engine.api.constraints.IIfElseConstraint;
import org.emoflon.neo.engine.api.rules.IMatch;

/**
 * Class representing an Implaction (if/then) constraint, storing all relevant
 * data, creates and runs the query for checking the constraint
 * 
 * @author Jannik Hinz
 *
 */
public class NeoImplication implements IIfElseConstraint {

	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	private Optional<IBuilder> builder;
	private NeoHelper helper;

	private AtomicPattern apIf;
	private AtomicPattern apThen;
	private String name;
	private List<NeoNode> nodesIf;
	private List<NeoNode> nodesThen;

	private boolean injective;

	/**
	 * 
	 * @param apIf      AtomicPattern of the If-Clause
	 * @param apThen    AtomicPattern of the Then-Clause
	 * @param injective boolean if the pattern should be matches injective or not
	 * @param builder   for creating and running Cypher queries
	 * @param helper    for creating nodes and relation with a unique name and
	 *                  central node storage
	 */
	public NeoImplication(AtomicPattern apIf, AtomicPattern apThen, boolean injective, Optional<IBuilder> builder,
			NeoHelper helper) {
		this.builder = builder;
		this.helper = helper;
		this.name = "IF " + apIf.getName() + " THEN " + apThen.getName();
		this.injective = injective;

		this.apIf = helper.getFlattenedPattern(apIf);
		this.apThen = helper.getFlattenedPattern(apThen);

		// Extracts all necessary information data from the Atomic Pattern
		this.nodesIf = new ArrayList<>();
		this.nodesIf = this.helper.extractNodesAndRelations(apIf.getNodeBlocks());
		this.nodesThen = new ArrayList<>();
		this.nodesThen = this.helper.extractNodesAndRelations(apThen.getNodeBlocks());
	}

	public NeoImplication(AtomicPattern apIf, AtomicPattern apThen, boolean injective, IBuilder builder,
			NeoHelper helper) {
		this(apIf, apThen, injective, Optional.of(builder), helper);
	}

	/**
	 * Return the name of the If/Then Constraint
	 * 
	 * @return name of the If/Then constraint
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the If-Clause Atomic Pattern (Premise)
	 * 
	 * @return AtomicPattern of the If-Clause (Premise)
	 */
	public AtomicPattern getIfPattern() {
		return apIf;
	}

	/**
	 * Return the Then-Clause Automic Pattern
	 * 
	 * @return AtomicPattern of the Then-Clause
	 */
	public AtomicPattern getThenPattern() {
		return apThen;
	}

	/**
	 * Returns a collection of the Nodes from the If-Clause
	 * 
	 * @return NeoNode collection of the Nodes from the If-Clause
	 */
	public Collection<NeoNode> getIfNodes() {
		return nodesIf;
	}

	/**
	 * Return a collection of the Nodes from the Then-Clause
	 * 
	 * @return NeoNode collection of the Nodes from the Then-Clause
	 */
	public Collection<NeoNode> getThenNodes() {
		return nodesThen;
	}

	/**
	 * Return a collection of all nodes from the If- and the Then-Clause
	 * 
	 * @return NeoNode collection of all nodes from the If- and the Then-Clause
	 */
	public Collection<NeoNode> getNodes() {
		var list = nodesIf;
		nodesThen.forEach(elem -> list.add(elem));
		return list;
	}

	/**
	 * Runs the Matching Query for If/Then Constraints and checks is the constraints
	 * is satisfied
	 * 
	 * @return true if the pattern matcher not find any violation in the then clause
	 *         and else false
	 */
	@Override
	public boolean isSatisfied() {

		if (getViolations() == null)
			return true;
		else
			return false;

	}

	/**
	 * Creates and runs the Query in the database for checking the if/then
	 * constraint violation
	 * 
	 * @return NeoMatches return a list of violating Matches of the constraint
	 */
	@Override
	public Collection<IMatch> getViolations() {

		var bld = builder.orElseThrow();

		logger.info("Check constraint: " + name);

		// create query
		var cypherQuery = CypherPatternBuilder.constraint_ifThen_readQuery(nodesIf, nodesThen, helper.getNodes(),
				injective);
		logger.debug(cypherQuery);

		// execute query
		var result = bld.executeQuery(cypherQuery);

		// analyze and return results
		var matches = new ArrayList<IMatch>();
		while (result.hasNext()) {
			matches.add(new NeoConstraintMatch(nodesIf, result.next()));
		}

		if (matches.isEmpty()) {
			logger.info("No invalid matches found. Constraint: " + name + " is complied!");
			return null;
		} else {
			logger.info("Invalid matches found. Constraint: " + name + " is NOT complied!");
			return matches;
		}
	}

	public String getQuery() {

		return CypherPatternBuilder.constraint_ifThen_readQuery(nodesIf, nodesThen, helper.getNodes(), injective);
	}
}
