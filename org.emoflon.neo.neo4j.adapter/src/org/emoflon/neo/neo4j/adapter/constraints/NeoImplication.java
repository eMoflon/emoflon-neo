package org.emoflon.neo.neo4j.adapter.constraints;

import java.util.List;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.neo4j.adapter.common.NeoNode;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.models.NeoCoreBuilder;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMask;
import org.emoflon.neo.neo4j.adapter.templates.CypherPatternBuilder;
import org.emoflon.neo.neo4j.adapter.util.NeoQueryData;
import org.emoflon.neo.neo4j.adapter.util.NeoUtil;
import org.neo4j.driver.v1.exceptions.DatabaseException;

/**
 * Class representing an Implication (if/then) constraint, storing all relevant
 * data, creates and runs the query for checking the constraint
 * 
 * @author Jannik Hinz
 *
 */
public class NeoImplication extends NeoConstraint {
	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);

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
	 * @param queryData    for creating nodes and relation with a unique name and
	 *                  central node storage
	 */
	public NeoImplication(AtomicPattern apIf, AtomicPattern apThen, boolean injective, IBuilder builder,
			NeoQueryData queryData, NeoMask mask) {
		super(builder, queryData, mask, injective);

		this.name = "IF " + apIf.getName() + " THEN " + apThen.getName();
		var flatIf = NeoUtil.getFlattenedPattern(apIf);
		var flatThen = NeoUtil.getFlattenedPattern(apThen);

		// Extracts all necessary information data from the Atomic Pattern
		this.nodesIf = this.queryData.extractConstraintNodesAndRelations(flatIf.getNodeBlocks());
		this.nodesThen = this.queryData.extractConstraintNodesAndRelations(flatThen.getNodeBlocks());
		this.nodesThenButNotIf = NeoUtil.extractElementsOnlyInConclusionPattern(this.nodesIf, this.nodesThen);
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
				nodesThenButNotIf, queryData.getAllElements(), queryData.getAttributeExpressionsOptional(), injective, mask);
		logger.debug(cypherQuery);

		// execute query
		var result = builder.executeQuery(cypherQuery);

		if(result == null) {
			throw new DatabaseException("400", "Execution Error: See console log for more details.");
		} else {
			// analyze and return results
			if (!result.hasNext()) {
				logger.info("No invalid matches found. Constraint: " + name + " is complied!");
				return true;
			} else {
				logger.info("Invalid matches found. Constraint: " + name + " is NOT complied!");
				return false;
			}
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
				queryData.getAllElements(), queryData.getAttributeExpressionsOptional(), injective, mask);
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
