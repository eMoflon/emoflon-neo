package org.emoflon.neo.neo4j.adapter.constraints;

import org.emoflon.neo.emsl.eMSL.AndBody;
import org.emoflon.neo.emsl.eMSL.ConstraintReference;
import org.emoflon.neo.emsl.eMSL.OrBody;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMask;
import org.emoflon.neo.neo4j.adapter.util.NeoHelper;

/**
 * Class for creating nested AndBodies used in NeoConstraints or NeoConditions
 * for proofing directing query fragments of constraints or conditions, and
 * calculating if they satisfy, getting the matches
 * 
 * @author Jannik Hinz
 *
 */
public class NeoAndBody extends NeoConstraint {
	private AndBody body;

	/**
	 * @param body    of the current AndBody
	 * @param builder for creating and running Cypher queries
	 * @param helper  for creating nodes and relation with a unique name and central
	 *                node storage
	 */
	public NeoAndBody(AndBody body, IBuilder builder, NeoHelper helper, NeoMask mask, boolean injective) {
		super(builder, helper, mask, injective);
		this.body = body;
	}

	/**
	 * Returns a NeoReturn Object with data and nodes in the nested constraints or
	 * Or-Bodies
	 * 
	 * @return NeoReturn Object with data and nodes in the nested constraints or
	 *         Or-Bodies
	 */
	@Override
	public NeoReturn getConstraintData() {
		NeoReturn returnStmt = new NeoReturn();
		var query = "";

		for (Object b : body.getChildren()) {

			if (!query.equals("")) {
				query += " AND ";
			}

			if (b instanceof ConstraintReference) {
				var constraintReference = (ConstraintReference) b;
				var consRef = NeoConstraintFactory.createNeoConstraint(constraintReference.getReference(), builder, helper, mask);
				var consData = consRef.getConstraintData();

				returnStmt.addNodes(consData.getNodes());
				returnStmt.addOptionalMatch(consData.getOptionalMatchString());

				if (constraintReference.isNegated()) {
					query += "NOT(" + consData.getWhereClause() + ")";
					consData.getIfThenWhere().forEach(elem -> returnStmt.addIfThenWhere("NOT " + elem));
				} else {
					query += "(" + consData.getWhereClause() + ")";
					consData.getIfThenWhere().forEach(elem -> returnStmt.addIfThenWhere(elem));
				}

			} else if (b instanceof OrBody) {
				var orbody = new NeoOrBody((OrBody) b, builder, helper, mask, injective);
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
	@Override
	public NeoReturn getConditionData() {
		NeoReturn returnStmt = new NeoReturn();
		var query = "";

		for (Object b : body.getChildren()) {

			if (!query.equals("")) {
				query += " AND ";
			}

			if (b instanceof ConstraintReference) {
				var constraintReference = (ConstraintReference) b;
				var consRef = NeoConstraintFactory.createNeoConstraint(constraintReference.getReference(), builder, helper, mask);
				var consData = consRef.getConditionData();

				returnStmt.addNodes(consData.getNodes());
				returnStmt.addOptionalMatch(consData.getOptionalMatchString());

				if (constraintReference.isNegated()) {
					query += "NOT(" + consData.getWhereClause() + ")";
					consData.getIfThenWhere().forEach(elem -> returnStmt.addIfThenWhere("NOT " + elem));
				} else {
					query += "(" + consData.getWhereClause() + ")";
					consData.getIfThenWhere().forEach(elem -> returnStmt.addIfThenWhere(elem));
				}

			} else if (b instanceof OrBody) {
				var orbody = new NeoOrBody((OrBody) b, builder, helper, mask, injective);
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

	@Override
	public String getName() {
		return "AND";
	}

}