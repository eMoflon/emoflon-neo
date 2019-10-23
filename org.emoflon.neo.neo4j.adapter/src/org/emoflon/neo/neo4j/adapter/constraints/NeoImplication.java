package org.emoflon.neo.neo4j.adapter.constraints;

import java.util.List;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.neo4j.adapter.common.NeoNode;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
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
	private static final Logger logger = Logger.getLogger(NeoImplication.class);

	private String name;
	private List<NeoNode> nodesIf;
	private List<NeoNode> nodesThen;
	private List<String> nodesThenButNotIf;

	public NeoImplication(//
			AtomicPattern apIf, //
			AtomicPattern apThen, //
			boolean injective, //
			IBuilder builder, //
			NeoQueryData queryData, //
			NeoMask mask) {
		super(builder, queryData, mask, injective);

		this.name = "IF " + apIf.getName() + " THEN " + apThen.getName();
		var flatIf = NeoUtil.getFlattenedPattern(apIf);
		var flatThen = NeoUtil.getFlattenedPattern(apThen);

		// Extracts all necessary information data from the Atomic Pattern
		this.nodesIf = this.queryData.extractPatternNodesAndRelations(flatIf.getNodeBlocks());
		this.nodesThen = this.queryData.extractConstraintNodesAndRelations(flatThen.getNodeBlocks());
		this.nodesThenButNotIf = NeoUtil.extractElementsOnlyInConclusionPattern(this.nodesIf, this.nodesThen);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isSatisfied() {
		logger.info("Check constraint: " + name);

		// create query
		var cypherQuery = getQuery();
		logger.debug(cypherQuery);

		// execute query
		var result = builder.executeQuery(cypherQuery);

		if (result == null) {
			throw new DatabaseException("400", "Execution Error: See console log for more details.");
		} else {
			if (!result.hasNext()) {
				logger.info("No invalid matches found. Constraint: " + name + " is satisfied!");
				return true;
			} else {
				logger.info("Invalid matches found. Constraint: " + name + " is NOT satisfied!");
				return false;
			}
		}
	}

	@Override
	public String getQuery() {
		return CypherPatternBuilder.constraint_ifThen_readQuery_satisfy(//
				nodesIf, //
				nodesThen, //
				nodesThenButNotIf, //
				queryData.getAllElements(), //
				queryData.getAttributeExpressions(), //
				queryData.getAttributeExpressionsOptional(), //
				queryData.getEqualElements(), //
				queryData.getAllNodesRequireInjectivityChecksCondition(), //
				injective, //
				mask);
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
