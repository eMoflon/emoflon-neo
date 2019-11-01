package org.emoflon.neo.neo4j.adapter.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import org.emoflon.neo.cypher.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMask;
import org.emoflon.neo.neo4j.adapter.util.NeoQueryData;

public abstract class NeoCombinator extends NeoConstraint {

	private Collection<NeoConstraint> children;
	private String combinator;

	public NeoCombinator(String combinator, Collection<?> children, IBuilder builder, NeoQueryData queryData, NeoMask mask, boolean injective) {
		super(builder, queryData, mask, injective);
		this.combinator = combinator;
		this.children = new ArrayList<>();
		computeStructure(children);
		this.returnAsCondition = computeReturnAsCondition();
		this.returnAsConstraint = computeReturnAsConstraint();
	}

	private void computeStructure(Collection<?> children) {
		for (var b : children)
			this.children.add(NeoConstraintFactory.createNeoConstraint(b, builder, queryData, mask, injective));
	}

	private NeoReturn computeReturn(Collection<NeoReturn> returns) {
		NeoReturn returnStmt = new NeoReturn();
		var query = "";

		for (var consData : returns) {
			if (!query.equals("")) {
				query += " " + combinator + " ";
			}

			returnStmt.addNodes(consData.getNodes());
			returnStmt.addOptionalMatch(consData.getOptionalMatchString());

			consData.getIfThenWhere().forEach(elem -> returnStmt.addIfThenWhere(elem));

			query += consData.getWhereClause();
		}

		returnStmt.addWhereClause("(" + query + ")");
		return returnStmt;
	}

	private NeoReturn computeReturnAsConstraint() {
		return computeReturn(children.stream().map(c -> c.getConstraintData()).collect(Collectors.toList()));
	}

	private NeoReturn computeReturnAsCondition() {
		return computeReturn(children.stream().map(c -> c.getConditionData()).collect(Collectors.toList()));
	}
}
