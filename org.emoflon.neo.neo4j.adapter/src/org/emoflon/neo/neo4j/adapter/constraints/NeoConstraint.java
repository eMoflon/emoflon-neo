package org.emoflon.neo.neo4j.adapter.constraints;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.neo4j.adapter.common.NeoNode;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.models.NeoCoreBuilder;
import org.emoflon.neo.neo4j.adapter.patterns.EmptyMask;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMask;
import org.emoflon.neo.neo4j.adapter.templates.CypherPatternBuilder;
import org.emoflon.neo.neo4j.adapter.util.NeoHelper;

/**
 * Class created, when a constraint should be checked or a constraint/condition
 * body is detected in nested constraint/condition tree. Runs relevant
 * constraint matching checks and collect or created the relevant data for
 * checking the constraint or their nested ones.
 * 
 * @author Jannik Hinz
 */
public abstract class NeoConstraint implements IConstraint {
	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);

	protected IBuilder builder;
	protected NeoHelper helper;
	protected NeoMask mask;
	protected final boolean injective;

	/**
	 * Constructor will be executed, if the NeoConstraint is created parent
	 * constraint
	 * 
	 * @param c       given Constraint for extracting the data
	 * @param builder for creating and running Cypher queries
	 * @param helper  for creating nodes and relation with a unique name and central
	 *                node storage
	 */
	protected NeoConstraint(IBuilder builder, NeoHelper helper, NeoMask mask, boolean injective) {
		this.builder = builder;
		this.helper = helper;
		this.mask = mask;
		this.injective = injective;
	}

	/**
	 * Constructor will be executed, if the NeoConstraint is created from the test
	 * 
	 * @param c              given Constraint for extracting the data
	 * @param neoCoreBuilder for creating and running Cypher queries
	 */
	protected NeoConstraint(IBuilder builder, NeoMask mask) {
		this(builder, new NeoHelper(), mask, true);
	}

	protected NeoConstraint(IBuilder builder) {
		this(builder, new EmptyMask());
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
	public abstract NeoReturn getConstraintData();

	/**
	 * Returns a NeoReturn Object with data and nodes from the condition or of the
	 * nested conditions or Or-Bodies
	 * 
	 * @return NeoReturn Object with data and nodes from the condition or of the
	 *         nested condition or Or-Bodies
	 */
	public abstract NeoReturn getConditionData();

	/**
	 * Runs the created Cypher query of all nested constraints and conditions an
	 * return if they satisfy or not
	 * 
	 * @return boolean true iff the complete nested Body and referenced conditions
	 *         satisfy or false if not
	 */
	@Override
	public boolean isSatisfied() {
		logger.info("Check constraint: " + getName());
		NeoReturn returnStmt = getConstraintData();

		logger.info("Searching matches for Constraint: " + getName());

		var cypherQuery = CypherPatternBuilder.constraintQuery_Satisfied(returnStmt.getOptionalMatchString(),
				returnStmt.getWhereClause());

		logger.debug(cypherQuery);
		var result = builder.executeQuery(cypherQuery);

		if (result.hasNext()) {
			logger.info("Found matches! Constraint: " + getName() + " is satisfied!");
			return true;
		} else {
			logger.info("Not matches found! Constraint: " + getName() + " is NOT satisfied!");
			return false;
		}
	}

	public String getQuery() {
		NeoReturn returnStmt = getConstraintData();
		return CypherPatternBuilder.constraintQuery_Satisfied(returnStmt.getOptionalMatchString(),
				returnStmt.getWhereClause());
	}

	protected NeoReturn createReturnStatement(Collection<NeoNode> nodes, String optionalQuery, String whereClause) {
		var returnStmt = new NeoReturn();
		returnStmt.addNodes(nodes);
		returnStmt.addOptionalMatch(optionalQuery);
		returnStmt.addWhereClause(whereClause);
		return returnStmt;
	}
}
