package org.emoflon.neo.neo4j.adapter.constraints;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.emoflon.neo.cypher.models.IBuilder;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.neo4j.adapter.common.NeoNode;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMask;
import org.emoflon.neo.neo4j.adapter.templates.CypherPatternBuilder;
import org.emoflon.neo.neo4j.adapter.util.NeoQueryData;
import org.neo4j.driver.v1.exceptions.DatabaseException;

/**
 * Class created, when a constraint should be checked or a constraint/condition
 * body is detected in nested constraint/condition tree. Runs relevant
 * constraint matching checks and collect or created the relevant data for
 * checking the constraint or their nested ones.
 * 
 * @author Jannik Hinz
 */
public abstract class NeoConstraint implements IConstraint {
	private static final Logger logger = Logger.getLogger(NeoConstraint.class);

	protected IBuilder builder;
	protected NeoQueryData queryData;
	protected NeoMask mask;
	protected final boolean injective;

	protected NeoReturn returnAsConstraint;
	protected NeoReturn returnAsCondition;

	/**
	 * Constructor will be executed, if the NeoConstraint is created parent
	 * constraint
	 * 
	 * @param c         given Constraint for extracting the data
	 * @param builder   for creating and running Cypher queries
	 * @param queryData for creating nodes and relation with a unique name and
	 *                  central node storage
	 */
	protected NeoConstraint(IBuilder builder, NeoQueryData queryData, NeoMask mask, boolean injective) {
		this.builder = builder;
		this.queryData = queryData;
		this.mask = mask;
		this.injective = injective;
	}

	/**
	 * Return the name of the constraint
	 * 
	 * @return String name of the constraint
	 */
	public abstract String getName();

	/**
	 * Returns a NeoReturn Object with data and nodes from the constraint or of the
	 * nested constraints or Or-Bodies
	 * 
	 * @return NeoReturn Object with data and nodes from the constraint or of the
	 *         nested constraints or Or-Bodies
	 */
	public NeoReturn getConstraintData() {
		return returnAsConstraint;
	}

	/**
	 * Returns a NeoReturn Object with data and nodes from the condition or of the
	 * nested conditions or Or-Bodies
	 * 
	 * @return NeoReturn Object with data and nodes from the condition or of the
	 *         nested condition or Or-Bodies
	 */
	public NeoReturn getConditionData() {
		return returnAsCondition;
	}

	/**
	 * Runs the created Cypher query of all nested constraints and conditions an
	 * return if they satisfy or not
	 * 
	 * @return boolean true iff the complete nested Body and referenced conditions
	 *         satisfy or false if not
	 */
	@Override
	public boolean isSatisfied() {
		logger.debug("Check constraint: " + getName());
		NeoReturn returnStmt = getConstraintData();

		logger.debug("Searching matches for Constraint: " + getName());

		var cypherQuery = CypherPatternBuilder.constraintQuery_Satisfied(returnStmt.getOptionalMatchString(),
				returnStmt.getWhereClause());

		logger.debug(cypherQuery);
		var result = builder.executeQuery(cypherQuery);

		if (result == null) {
			throw new DatabaseException("400", "Execution Error: See console log for more details.");
		} else {
			if (result.hasNext()) {
				logger.debug("Found matches! Constraint: " + getName() + " is satisfied!");
				return true;
			} else {
				logger.debug("Not matches found! Constraint: " + getName() + " is NOT satisfied!");
				return false;
			}
		}
	}

	public String getQuery() {
		NeoReturn returnStmt = getConstraintData();
		return CypherPatternBuilder.constraintQuery_Satisfied(returnStmt.getOptionalMatchString(),
				returnStmt.getWhereClause());
	}

	protected NeoReturn createReturnStatement(Collection<NeoNode> nodes, String optionalQuery, String whereClause) {
		return createReturnStatement(nodes, optionalQuery, whereClause, "");
	}

	protected NeoReturn createReturnStatement(Collection<NeoNode> nodes, String optionalQuery, String whereClause,
			String whereEqualCond) {
		var returnStmt = new NeoReturn();
		returnStmt.addNodes(nodes);
		returnStmt.addOptionalMatch(optionalQuery);
		returnStmt.addWhereClause(whereClause);
		return returnStmt;
	}
}
