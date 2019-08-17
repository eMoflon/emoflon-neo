package org.emoflon.neo.neo4j.adapter;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.Constraint;
import org.emoflon.neo.emsl.eMSL.Implication;
import org.emoflon.neo.emsl.eMSL.NegativeConstraint;
import org.emoflon.neo.emsl.eMSL.OrBody;
import org.emoflon.neo.emsl.eMSL.PositiveConstraint;
import org.emoflon.neo.engine.api.constraints.IConstraint;

/**
 * Class created, when a constraint should be checked or a constraint/condition
 * body is detected in nested constraint/condition tree. Runs relevant
 * constraint matching checks and collect or created the relevant data for
 * checking the constraint or their nested ones.
 * 
 * @author Jannik Hinz
 */
public class NeoConstraint implements IConstraint {
	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	private IBuilder builder;
	private NeoHelper helper;
	private Constraint c;
	private NeoMask mask;

	private final boolean injective = true;

	/**
	 * Constructor will be executed, if the NeoConstraint is created from the test
	 * 
	 * @param c              given Constraint for extracting the data
	 * @param neoCoreBuilder for creating and running Cypher queries
	 */
	public NeoConstraint(Constraint c, IBuilder builder, NeoMask mask) {
		this.builder = builder;
		this.helper = new NeoHelper();
		this.c = c;
		this.mask = mask;
	}

	public NeoConstraint(Constraint c, IBuilder builder) {
		this(c, builder, new EmptyMask());
	}

	/**
	 * Constructor will be executed, if the NeoConstraint is created parent
	 * constraint
	 * 
	 * @param c       given Constraint for extracting the data
	 * @param builder for creating and running Cypher queries
	 * @param helper  for creating nodes and relation with a unique name and central
	 *                node storage
	 */
	public NeoConstraint(Constraint c, IBuilder builder, NeoHelper helper, NeoMask mask) {
		this.builder = builder;
		this.helper = helper;
		this.c = c;
		this.mask = mask;
	}

	/**
	 * Return the name of the constraint
	 * 
	 * @return String name of the constraint
	 */
	public String getName() {
		return c.getName();
	}

	/**
	 * Returns a NeoReturn Object with data and nodes from the constraint or of the
	 * nested constraints or Or-Bodies
	 * 
	 * @return NeoReturn Object with data and nodes from the constraint or of the
	 *         nested constraints or Or-Bodies
	 */
	public NeoReturn getConstraintData() {

		NeoReturn returnStmt = new NeoReturn();

		if (c.getBody() instanceof PositiveConstraint) {
			var ap = ((PositiveConstraint) c.getBody()).getPattern();
			ap = NeoHelper.getFlattenedPattern(ap);
			var co = new NeoPositiveConstraint(ap, injective, builder, helper, mask);

			returnStmt.addNodes(co.getNodes());
			returnStmt.addOptionalMatch(co.getQueryString_MatchConstraint());
			returnStmt.addWhereClause(co.getQueryString_WhereConstraint());

		} else if (c.getBody() instanceof NegativeConstraint) {
			var ap = ((NegativeConstraint) c.getBody()).getPattern();
			ap = NeoHelper.getFlattenedPattern(ap);
			var co = new NeoNegativeConstraint(ap, injective, builder, helper, mask);

			returnStmt.addNodes(co.getNodes());
			returnStmt.addOptionalMatch(co.getQueryString_MatchConstraint());
			returnStmt.addWhereClause(co.getQueryString_WhereConstraint());

		} else if (c.getBody() instanceof OrBody) {

			var body = (OrBody) c.getBody();
			var neoBody = new NeoOrBody(body, builder, helper, mask);

			returnStmt = neoBody.getConstraintData();

		} else {
			logger.info("Its an Unkown Type!");
			throw new UnsupportedOperationException(c.getBody().toString());
		}

		return returnStmt;
	}

	/**
	 * Returns a NeoReturn Object with data and nodes from the condition or of the
	 * nested conditions or Or-Bodies
	 * 
	 * @return NeoReturn Object with data and nodes from the condition or of the
	 *         nested condition or Or-Bodies
	 */
	public NeoReturn getConditionData() {

		NeoReturn returnStmt = new NeoReturn();

		if (c.getBody() instanceof PositiveConstraint) {
			var ap = ((PositiveConstraint) c.getBody()).getPattern();
			ap = NeoHelper.getFlattenedPattern(ap);
			var co = new NeoPositiveConstraint(ap, injective, builder, helper, mask);

			returnStmt.addNodes(co.getNodes());
			returnStmt.addOptionalMatch(co.getQueryString_MatchCondition());
			returnStmt.addWhereClause(co.getQueryString_WhereCondition());

		} else if (c.getBody() instanceof NegativeConstraint) {
			var ap = ((NegativeConstraint) c.getBody()).getPattern();
			ap = NeoHelper.getFlattenedPattern(ap);
			var co = new NeoNegativeConstraint(ap, injective, builder, helper, mask);

			returnStmt.addNodes(co.getNodes());
			returnStmt.addOptionalMatch(co.getQueryString_MatchCondition());
			returnStmt.addWhereClause(co.getQueryString_WhereConditon());

		} else if (c.getBody() instanceof OrBody) {

			var body = (OrBody) c.getBody();
			var neoBody = new NeoOrBody(body, builder, helper, mask);

			returnStmt = neoBody.getConditionData();

		} else {
			logger.info("Its an Unkown Type!");
			throw new UnsupportedOperationException(c.getBody().toString());
		}

		return returnStmt;
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
		if (c.getBody() instanceof Implication) {
			var implication = (Implication) c.getBody();
			var apIf = implication.getPremise();
			var apThen = implication.getConclusion();
			apIf = NeoHelper.getFlattenedPattern(apIf);
			apThen = NeoHelper.getFlattenedPattern(apThen);
			var co = new NeoImplication(apIf, apThen, injective, builder, helper, mask);

			return co.isSatisfied();

		} else {

			logger.info("Check constraint: " + c.getName());
			NeoReturn returnStmt = getConstraintData();

			logger.info("Searching matches for Constraint: " + c.getName());

			var cypherQuery = CypherPatternBuilder.constraintQuery_Satisfied(returnStmt.getOptionalMatchString(),
					returnStmt.getWhereClause());

			logger.debug(cypherQuery);
			var result = builder.executeQuery(cypherQuery);

			if (result.hasNext()) {
				logger.info("Found matches! Constraint: " + c.getName() + " is satisfied!");
				return true;
			} else {
				logger.info("Not matches found! Constraint: " + c.getName() + " is NOT satisfied!");
				return false;
			}
		}

	}

	public String getQuery() {

		if (c.getBody() instanceof Implication) {
			var implication = (Implication) c.getBody();
			var apIf = implication.getPremise();
			var apThen = implication.getConclusion();
			apIf = NeoHelper.getFlattenedPattern(apIf);
			apThen = NeoHelper.getFlattenedPattern(apThen);
			var co = new NeoImplication(apIf, apThen, injective, builder, helper, mask);

			return co.getQuery();
		} else {
			NeoReturn returnStmt = getConstraintData();
			return CypherPatternBuilder.constraintQuery_Satisfied(returnStmt.getOptionalMatchString(),
					returnStmt.getWhereClause());
		}

	}

}
