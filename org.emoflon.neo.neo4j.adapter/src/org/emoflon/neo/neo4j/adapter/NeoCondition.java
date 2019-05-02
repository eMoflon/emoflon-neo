package org.emoflon.neo.neo4j.adapter;

import org.emoflon.neo.emsl.eMSL.ConditionOperator;

public class NeoCondition {

	private String name;
	private String op;
	private boolean opNeg;
	private String value;

	private String classVarName;

	public NeoCondition(String name, ConditionOperator op, String value, String classVarName) {

		this.classVarName = classVarName;

		this.name = name;
		this.value = value;
		this.opNeg = false;
		convertOp(op);
	}

	@Override
	public String toString() {
		return CypherPatternBuilder.cypherCondition(name, op, opNeg, value, classVarName);
	}

	private void convertOp(ConditionOperator opcode) {

		switch (opcode) {
		case EQ:
			this.op = "=";
			break;
		case GREATER:
			this.op = ">";
			break;
		case GREATEREQ:
			this.op = ">=";
			break;
		case LESS:
			this.op = "<";
			break;
		case LESSEQ:
			this.op = "<=";
			break;
		case NOTEQ:
			this.op = "=";
			this.opNeg = true;
			break;
		default:
			throw new IllegalArgumentException("Unknown condition operator: " + opcode);
		}

	}

}
