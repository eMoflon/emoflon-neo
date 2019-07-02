package org.emoflon.neo.neo4j.adapter;

import org.emoflon.neo.emsl.eMSL.AndBody;
import org.emoflon.neo.emsl.eMSL.OrBody;

/*
 * Class for creating nested AndBodies used in NeoConstraints or NeoConditions
 * for proofing if a constraint or condition is satisfied
 */
public class NeoOrBody {

	private OrBody body;
	private NeoCoreBuilder builder;

	/*
	 * @param body of the current OrBody
	 * @param builder for creating and running Cypher queries
	 */
	public NeoOrBody(OrBody body, NeoCoreBuilder builder) {

		this.body = body;
		this.builder = builder;

	}

	/*
	 * Calculates and creates the nested AND constraints bodies and conditions an
	 * return if they satisfy or not
	 * 
	 * @return boolean true iff the complete nested AND Body and their child satisfy
	 * or false if not
	 */
	public boolean isSatisfied() {

		// for all child in the constraint body
		for (AndBody b : body.getChildren()) {
			var andbody = new NeoAndBody(b, builder);

			if (andbody.isSatisfied()) {
				return true;
			}
		}

		return false;

	}

	/*
	 * Returns a NeoNode Collection of all nodes in the nested constraint or body
	 * 
	 * @return NeoNode Collection of all nodes in the nested constraint or body
	 */
	public NeoReturn getConstraintData() {

		NeoReturn returnStmt = new NeoReturn();
		var query = "";

		for (AndBody b : body.getChildren()) {
			var andbody = new NeoAndBody(b, builder);
			returnStmt.addNodes(andbody.getConstraintData().getNodes());
			returnStmt.addOptionalMatch(andbody.getConstraintData().getOptionalMatchString());
			
			if (!query.equals("")) {
				query += " OR ";
			}
			query += andbody.getConstraintData().getWhereClause();
			
		}
		returnStmt.addWhereClause("(" + query + ")");
		return returnStmt;

	}
	
}
