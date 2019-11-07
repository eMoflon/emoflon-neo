package org.emoflon.neo.cypher.common;

import org.emoflon.neo.emsl.eMSL.ConditionOperator;
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;

public class NeoAssertion extends NeoProperty {
	private String operator;

	public NeoAssertion(ModelPropertyStatement prop, NeoElement element) {
		super(prop, element);
		operator = interpretOp(prop.getOp());
	}

	private String interpretOp(ConditionOperator op) {
		switch (op) {
		case GREATER:
			return ">";
		case LESS:
			return "<";
		case GREATEREQ:
			return ">=";
		case LESSEQ:
			return "<=";
		case NOTEQ:
			return "<>";

		default:
			throw new IllegalArgumentException("Unexpected value: " + op);
		}
	}

	public String getOperator() {
		return operator;
	}
}
