package org.emoflon.neo.neo4j.adapter;

import org.emoflon.neo.emsl.eMSL.NodeBlockConditionOperator;

public class NeoCondition {
	
	private String name;
	private String op;
	private boolean opNeg;
	private String value;
	
	private String classVarName;

	public NeoCondition(String name, NodeBlockConditionOperator op, String value, String classVarName) {
		
		this.classVarName = classVarName;
		
		this.name = name;
		this.value = value;
		this.opNeg = false;
		convertOp(op);
		
	}
	
	@Override
	public String toString() {
		String result = "";
		if(opNeg) {
			result += "NOT";
		}
		return result + " " + classVarName + "." + name + " " + op + " \"" + value + "\"";
	}
	
	private void convertOp(NodeBlockConditionOperator opcode) {
		
		switch(opcode.toString()) {
		case "EQ_VALUE":
		case "EQ":
		case "==":
			this.op = "=";
			break;
		case "GREATER_VALUE":
		case "GREATER":
		case ">":
			this.op = ">";
			break;
		case "GREATEREQ_VALUE":
		case "GREATEREQ":
		case ">=":
			this.op = ">=";
			break;
		case "LESS_VALUE":
		case "LESS":
		case "<":
			this.op = "<";
			break;
		case "LESSEQ_VALUE":
		case "LESSEQ":
		case "<=":
			this.op = "<=";
			break;
		case "NOTEQ":
		case "NOTEQ_VALUE":
		case "!=":
		default:
			this.op = "<=";
			this.opNeg = true;
		}
		
	}

}
