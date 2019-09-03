package org.emoflon.neo.neo4j.adapter.constraints;

import org.emoflon.neo.emsl.eMSL.ConstraintReference;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMask;
import org.emoflon.neo.neo4j.adapter.util.NeoQueryData;

public class NeoConstraintRef extends NeoConstraint {

	private NeoConstraint constraintRef;
	private boolean isNegated;
	
	protected NeoConstraintRef(ConstraintReference ref, IBuilder builder, NeoQueryData queryData, NeoMask mask, boolean injective) {
		super(builder, queryData, mask, injective);
		isNegated = ref.isNegated();
		constraintRef = NeoConstraintFactory.createNeoConstraint(ref.getReference(), builder, queryData, mask, injective);
		this.returnAsCondition = computeReturnAsCondition();
		this.returnAsConstraint = computeReturnAsConstraint();
	}

	private NeoReturn computeReturn(NeoReturn consData) {
		var returnStmt = new NeoReturn();
		var query = "";
		
		returnStmt.addNodes(consData.getNodes());
		returnStmt.addOptionalMatch(consData.getOptionalMatchString());
	
		if (isNegated) {
			query += "NOT(" + consData.getWhereClause() + ")";
			consData.getIfThenWhere().forEach(elem -> returnStmt.addIfThenWhere("NOT " + elem));
		} else {
			query += "(" + consData.getWhereClause() + ")";
			consData.getIfThenWhere().forEach(elem -> returnStmt.addIfThenWhere(elem));
		}
		
		returnStmt.addWhereClause(query);
		return returnStmt;
	}

	private NeoReturn computeReturnAsCondition() {
		return computeReturn(constraintRef.returnAsCondition);
	}
	
	private NeoReturn computeReturnAsConstraint() {
		return computeReturn(constraintRef.returnAsConstraint);
	}

	@Override
	public String getName() {
		return "REF(...)";
	}
}
