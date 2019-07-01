package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;

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
						|| satisfied && ((ConstraintReference) b).isNegated()) {

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
	public Collection<NeoNode> getNodes() {

		Collection<NeoNode> nodes = new ArrayList<>();

		for (Object b : body.getChildren()) {

			if (b instanceof ConstraintReference) {
				var consRef = new NeoConstraint(((ConstraintReference) b).getReference(), builder);

				for (NeoNode node : consRef.getNodes()) {
					nodes.add(node);
				}

			} else if (b instanceof OrBody) {
				var orbody = new NeoOrBody((OrBody) b, builder);

				for (NeoNode node : orbody.getNodes()) {
					nodes.add(node);
				}
			}
		}

		return nodes;

	}

	/*
	 * Returns the OPTIONAL MATCH cypher string for all nested bodies and
	 * constraints
	 * 
	 * @return String OPTIONAL MATCH cypher string of all nested bodies and
	 * constraints
	 */
	public String getOptionalMatch() {

		var query = "";

		for (Object b : body.getChildren()) {

			if (b instanceof ConstraintReference) {
				var consRef = new NeoConstraint(((ConstraintReference) b).getReference(), builder);
				query += consRef.getOptionalQuery();

			} else if (b instanceof OrBody) {
				var orbody = new NeoOrBody((OrBody) b, builder);
				query += orbody.getOptionalQuery();
			}
		}

		return query;
	}

	/*
	 * Returns the WHERE cypher string for all nested bodies and constraints
	 * 
	 * @return String WHERE cypher string of all nested bodies and constraints
	 */
	public String getQueryString_Where() {

		var query = "(";
		var first = true;

		for (Object b : body.getChildren()) {

			if (b instanceof ConstraintReference) {
				var consRef = new NeoConstraint(((ConstraintReference) b).getReference(), builder);
				if (first) {
					first = false;
				} else {
					query += " AND ";
				}
				if (((ConstraintReference) b).isNegated())
					query += "NOT(" + consRef.getWhereQuery() + ")";
				else
					query += "(" + consRef.getWhereQuery() + ")";

			} else if (b instanceof OrBody) {
				var orbody = new NeoOrBody((OrBody) b, builder);
				if (first) {
					first = false;
				} else {
					query += " OR ";
				}
				query += orbody.getQueryString_Where();
			}
		}

		return query + ")";
	}

}
