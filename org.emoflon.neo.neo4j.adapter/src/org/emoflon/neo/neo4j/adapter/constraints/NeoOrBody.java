package org.emoflon.neo.neo4j.adapter.constraints;

import org.emoflon.neo.emsl.eMSL.AndBody;
import org.emoflon.neo.emsl.eMSL.OrBody;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMask;
import org.emoflon.neo.neo4j.adapter.util.NeoQueryData;

/**
 * Class for creating nested AndBodies used in NeoConstraints or NeoConditions
 * for proofing if a constraint or condition is satisfied
 * 
 * @author Jannik Hinz
 *
 */
public class NeoOrBody extends NeoConstraint {
	private OrBody body;
	
	/**
	 * @param body    of the current OrBody
	 * @param builder for creating and running Cypher queries
	 * @param helper  for creating nodes and relation with a unique name and central
	 *                node storage
	 */
	public NeoOrBody(OrBody body, IBuilder builder, NeoQueryData helper, NeoMask mask, boolean injective) {
		super(builder, helper, mask, injective);
		this.body = body;
	}

	/**
	 * Returns a NeoReturn Object with data and nodes in the nested And-Bodies
	 * 
	 * @return NeoReturn Object with data and nodes in the nested And-Bodies
	 */
	@Override
	public NeoReturn getConstraintData() {
		NeoReturn returnStmt = new NeoReturn();
		var query = "";

		for (AndBody b : body.getChildren()) {
			var andbody = new NeoAndBody(b, builder, helper, mask, injective);
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
	@Override
	public NeoReturn getConditionData() {
		NeoReturn returnStmt = new NeoReturn();
		var query = "";

		for (AndBody b : body.getChildren()) {
			var andbody = new NeoAndBody(b, builder, helper, mask, injective);
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

	@Override
	public String getName() {
		return "OR";
	}

}
