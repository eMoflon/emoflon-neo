package org.emoflon.neo.neo4j.adapter;

import java.util.Optional;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.AndBody;
import org.emoflon.neo.emsl.eMSL.ConstraintReference;
import org.emoflon.neo.emsl.eMSL.OrBody;

/**
 * Class for creating nested AndBodies used in NeoConstraints or NeoConditions
 * for proofing directing query fragments of constraints or conditions, and
 * calculating if they satisfy, getting the matches
 * 
 * @author Jannik Hinz
 *
 */
public class NeoAndBody {

	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	private AndBody body;
	private NeoCoreBuilder builder;
	private NeoHelper helper;

	/**
	 * @param body    of the current AndBody
	 * @param builder for creating and running Cypher queries
	 * @param helper  for creating nodes and relation with a unique name and central
	 *                node storage
	 */
	public NeoAndBody(AndBody body, NeoCoreBuilder builder, NeoHelper helper) {

		this.body = body;
		this.builder = builder;
		this.helper = helper;
	}

	/**
	 * Calculates and creates the nested constraints and conditions an return if
	 * they satisfy or not
	 * 
	 * @return boolean true iff the complete nested Body and referenced constraints
	 *         satisfy or false if not
	 */
	public boolean isSatisfied() {

		// for all child in the constraint body
		for (Object b : body.getChildren()) {

			// if its an constraint body, check if this constraint satisfies
			if (b instanceof ConstraintReference) {
				var consRef = new NeoConstraint(((ConstraintReference) b).getReference(), builder, helper);

				if (((ConstraintReference) b).isNegated()) {
					logger.info("Attention: Constraint is negated!");
				}

				var satisfied = consRef.isSatisfied();

				if ((!satisfied && !((ConstraintReference) b).isNegated())
						|| (satisfied && ((ConstraintReference) b).isNegated())) {

					return false;
				}

			}
			// if its an nested body, check if this nested body and its constraint satisfy
			else if (b instanceof OrBody) {
				var orbody = new NeoOrBody((OrBody) b, builder, helper);

				if (!orbody.isSatisfied()) {
					return false;
				}
			}
		}

		return true;

	}

	/**
	 * Returns a NeoReturn Object with data and nodes in the nested constraints or
	 * Or-Bodies
	 * 
	 * @return NeoReturn Object with data and nodes in the nested constraints or
	 *         Or-Bodies
	 */
	public NeoReturn getConstraintData() {

		NeoReturn returnStmt = new NeoReturn();
		var query = "";

		for (Object b : body.getChildren()) {

			if (!query.equals("")) {
				query += " AND ";
			}

			if (b instanceof ConstraintReference) {
				var consRef = new NeoConstraint(((ConstraintReference) b).getReference(), builder, helper);
				var consData = consRef.getConstraintData();

				returnStmt.addNodes(consData.getNodes());
				returnStmt.addOptionalMatch(consData.getOptionalMatchString());

				if (((ConstraintReference) b).isNegated()) {
					query += "NOT(" + consData.getWhereClause() + ")";
					consData.getIfThenWhere().forEach(elem -> returnStmt.addIfThenWhere("NOT " + elem));
				} else {
					query += "(" + consData.getWhereClause() + ")";
					consData.getIfThenWhere().forEach(elem -> returnStmt.addIfThenWhere(elem));
				}

			} else if (b instanceof OrBody) {
				var orbody = new NeoOrBody((OrBody) b, builder, helper);
				var consData = orbody.getConstraintData();
				returnStmt.addNodes(consData.getNodes());
				returnStmt.addOptionalMatch(consData.getOptionalMatchString());

				consData.getIfThenWhere().forEach(elem -> returnStmt.addIfThenWhere(elem));

				query += consData.getWhereClause();
			}
		}
		returnStmt.addWhereClause("(" + query + ")");
		return returnStmt;

	}

	/**
	 * Returns a NeoReturn Object with data and nodes in the nested conditions or
	 * Or-Bodies
	 * 
	 * @return NeoReturn Object with data and nodes in the nested conditions or
	 *         Or-Bodies
	 */
	public NeoReturn getConditionData() {

		NeoReturn returnStmt = new NeoReturn();
		var query = "";

		for (Object b : body.getChildren()) {

			if (!query.equals("")) {
				query += " AND ";
			}

			if (b instanceof ConstraintReference) {
				var consRef = new NeoConstraint(((ConstraintReference) b).getReference(), builder, helper);
				var consData = consRef.getConditionData();

				returnStmt.addNodes(consData.getNodes());
				returnStmt.addOptionalMatch(consData.getOptionalMatchString());

				if (((ConstraintReference) b).isNegated()) {
					query += "NOT(" + consData.getWhereClause() + ")";
					consData.getIfThenWhere().forEach(elem -> returnStmt.addIfThenWhere("NOT " + elem));
				} else {
					query += "(" + consData.getWhereClause() + ")";
					consData.getIfThenWhere().forEach(elem -> returnStmt.addIfThenWhere(elem));
				}

			} else if (b instanceof OrBody) {
				var orbody = new NeoOrBody((OrBody) b, builder, helper);
				var consData = orbody.getConditionData();
				returnStmt.addNodes(consData.getNodes());
				returnStmt.addOptionalMatch(consData.getOptionalMatchString());

				consData.getIfThenWhere().forEach(elem -> returnStmt.addIfThenWhere(elem));

				query += consData.getWhereClause();
			}
		}
		returnStmt.addWhereClause("(" + query + ")");
		return returnStmt;

	}

}