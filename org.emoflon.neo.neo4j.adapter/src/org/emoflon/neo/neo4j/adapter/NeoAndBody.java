package org.emoflon.neo.neo4j.adapter;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.AndBody;
import org.emoflon.neo.emsl.eMSL.ConstraintReference;
import org.emoflon.neo.emsl.eMSL.OrBody;

/*
 * Class for creating nested AndBodies used in NeoConstraints or NeoConditions
 * for proofing if a constraint or condition is satisfied
 */
public class NeoAndBody {

	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	private AndBody body;
	private NeoCoreBuilder builder;

	/*
	 * @param body of the current AndBody
	 * @param builder for creating and running cypher queries
	 */
	public NeoAndBody(AndBody body, NeoCoreBuilder builder) {

		this.body = body;
		this.builder = builder;

	}

	/*
	 * Calculates and creates the nested constraints and conditions an return if
	 * they satisfy or not
	 * 
	 * @return boolean true iff the complete nested Body and referenced conditions
	 * satisfy or false if not
	 */
	public boolean isSatisfied() {

		// for all child in the constraint body
		for (Object b : body.getChildren()) {

			// if its an constraint body, check if this constraint satisfies
			if (b instanceof ConstraintReference) {
				var consRef = new NeoConstraint(((ConstraintReference) b).getReference(), builder);

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
				var orbody = new NeoOrBody((OrBody) b, builder);

				if (!orbody.isSatisfied()) {
					return false;
				}
			}
		}

		return true;

	}

	/*
	 * Returns a NeoNode Collection of all nodes in the nested constraint or body
	 * 
	 * @return NeoNode Collection of all nodes in the nested constraint or body
	 */
	public NeoReturn getConstraintData() {
		
		NeoReturn returnStmt = new NeoReturn();
		var query = "";

		for (Object b : body.getChildren()) {
			
			if (!query.equals("")) {
				query += " AND ";
			}

			if (b instanceof ConstraintReference) {
				var consRef = new NeoConstraint(((ConstraintReference) b).getReference(), builder);
				returnStmt.addNodes(consRef.getConstraintData().getNodes());
				returnStmt.addOptionalMatch(consRef.getConstraintData().getOptionalMatchString());

				if (((ConstraintReference) b).isNegated())
					query += "NOT(" + consRef.getConstraintData().getWhereClause() + ")";
				else
					query += "(" + consRef.getConstraintData().getWhereClause() + ")";

			} else if (b instanceof OrBody) {
				var orbody = new NeoOrBody((OrBody) b, builder);
				returnStmt.addNodes(orbody.getConstraintData().getNodes());
				returnStmt.addOptionalMatch(orbody.getConstraintData().getOptionalMatchString());
				query += orbody.getConstraintData().getWhereClause();
			}
		}
		returnStmt.addWhereClause("(" + query + ")");
		return returnStmt;

	}

}	