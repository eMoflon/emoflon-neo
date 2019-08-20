package org.emoflon.neo.neo4j.adapter.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.neo4j.adapter.common.NeoNode;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.models.NeoCoreBuilder;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMask;
import org.emoflon.neo.neo4j.adapter.templates.CypherPatternBuilder;
import org.emoflon.neo.neo4j.adapter.util.NeoHelper;

/**
 * Class representing an Implication (if/then) constraint, storing all relevant
 * data, creates and runs the query for checking the constraint
 * 
 * @author Jannik Hinz
 *
 */
public class NeoImplication extends NeoConstraint {
	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);

	private AtomicPattern apIf;
	private AtomicPattern apThen;
	private String name;
	private List<NeoNode> nodesIf;
	private List<NeoNode> nodesThen;
	private List<String> nodesThenButNotIf;

	/**
	 * 
	 * @param apIf      AtomicPattern of the If-Clause
	 * @param apThen    AtomicPattern of the Then-Clause
	 * @param injective boolean if the pattern should be matches injective or not
	 * @param builder   for creating and running Cypher queries
	 * @param helper    for creating nodes and relation with a unique name and
	 *                  central node storage
	 */
	public NeoImplication(AtomicPattern apIf, AtomicPattern apThen, boolean injective, IBuilder builder,
			NeoHelper helper, NeoMask mask) {
		super(builder, helper, mask, injective);

		this.name = "IF " + apIf.getName() + " THEN " + apThen.getName();
		this.apIf = NeoHelper.getFlattenedPattern(apIf);
		this.apThen = NeoHelper.getFlattenedPattern(apThen);

		// Extracts all necessary information data from the Atomic Pattern
		this.nodesIf = this.helper.extractNodesAndRelations(apIf.getNodeBlocks());
		this.nodesThen = this.helper.extractNodesAndRelations(apThen.getNodeBlocks());
		this.nodesThenButNotIf = NeoHelper.extractElementsOnlyInConclusionPattern(this.nodesIf, this.nodesThen);
	}

	/**
	 * Return the name of the If/Then Constraint
	 * 
	 * @return name of the If/Then constraint
	 */
	@Override
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
		var list = new HashSet<>(nodesIf);
		list.addAll(nodesThen);
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
		logger.info("Check constraint: " + name);

		// create query
		var cypherQuery = CypherPatternBuilder.constraint_ifThen_readQuery_satisfy(nodesIf, nodesThen,
				nodesThenButNotIf, helper.getAllElements(), injective, mask);
		logger.debug(cypherQuery);

		// execute query
		var result = builder.executeQuery(cypherQuery);

		// analyze and return results
		var matches = new ArrayList<IMatch>();
		while (result.hasNext()) {
			matches.add(new NeoConstraintMatch(nodesIf, result.next()));
		}

		if (matches.isEmpty()) {
			logger.info("No invalid matches found. Constraint: " + name + " is complied!");
			return true;
		} else {
			logger.info("Invalid matches found. Constraint: " + name + " is NOT complied!");
			return false;
		}
	}

	/**
	 * Return the query for outline copy to clipboard function
	 * 
	 * @return String query for outline copy to clipboard
	 */
	@Override
	public String getQuery() {
		return CypherPatternBuilder.constraint_ifThen_readQuery_satisfy(nodesIf, nodesThen, nodesThenButNotIf,
				helper.getAllElements(), injective, mask);
	}

	@Override
	public NeoReturn getConstraintData() {
		throw new UnsupportedOperationException("Implications cannot be nested!");
	}

	@Override
	public NeoReturn getConditionData() {
		throw new UnsupportedOperationException("Implications cannot be used as conditions!");
	}
}
