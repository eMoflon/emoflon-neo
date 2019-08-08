package org.emoflon.neo.neo4j.adapter;

import org.emoflon.neo.emsl.eMSL.AndBody;
import org.emoflon.neo.emsl.eMSL.OrBody;

/**
 * Class for creating nested AndBodies used in NeoConstraints or NeoConditions
 * for proofing if a constraint or condition is satisfied
 * 
 * @author Jannik Hinz
 *
 */
public class NeoOrBody {

	private OrBody body;
	private IBuilder builder;
	private NeoHelper helper;
	private NeoMask mask;

	/**
	 * @param body    of the current OrBody
	 * @param builder for creating and running Cypher queries
	 * @param helper  for creating nodes and relation with a unique name and central
	 *                node storage
	 */
	public NeoOrBody(OrBody body, IBuilder builder, NeoHelper helper, NeoMask mask) {
		this.body = body;
		this.builder = builder;
		this.helper = helper;
		this.mask = mask;
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
		for (AndBody b : body.getChildren()) {
			var andbody = new NeoAndBody(b, builder, helper, mask);

			if (andbody.isSatisfied()) {
				return true;
			}
		}

		return false;

	}

	/**
	 * Returns a NeoReturn Object with data and nodes in the nested And-Bodies
	 * 
	 * @return NeoReturn Object with data and nodes in the nested And-Bodies
	 */
	public NeoReturn getConstraintData() {

		NeoReturn returnStmt = new NeoReturn();
		var query = "";

		for (AndBody b : body.getChildren()) {
			var andbody = new NeoAndBody(b, builder, helper, mask);
			var consData = andbody.getConstraintData();
			returnStmt.addNodes(consData.getNodes());
			returnStmt.addOptionalMatch(consData.getOptionalMatchString());

			if (!query.equals("")) {
				query += " OR ";
			}
			query += consData.getWhereClause();
			consData.getIfThenWhere().forEach(elem -> returnStmt.addIfThenWhere(elem));

		}
		returnStmt.addWhereClause("(" + query + ")");
		return returnStmt;

	}

	/**
	 * Returns a NeoReturn Object with data and nodes in the nested And-Bodies
	 * 
	 * @return NeoReturn Object with data and nodes in the nested And-Bodies
	 */
	public NeoReturn getConditionData() {

		NeoReturn returnStmt = new NeoReturn();
		var query = "";

		for (AndBody b : body.getChildren()) {
			var andbody = new NeoAndBody(b, builder, helper, mask);
			var consData = andbody.getConditionData();
			returnStmt.addNodes(consData.getNodes());
			returnStmt.addOptionalMatch(consData.getOptionalMatchString());

			if (!query.equals("")) {
				query += " OR ";
			}
			query += consData.getWhereClause();
			consData.getIfThenWith().forEach(elem -> returnStmt.addIfThenWith(elem));
			consData.getIfThenWhere().forEach(elem -> returnStmt.addIfThenWhere(elem));

		}
		returnStmt.addWhereClause("(" + query + ")");
		return returnStmt;

	}

}
