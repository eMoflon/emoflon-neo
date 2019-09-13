package org.emoflon.neo.neo4j.adapter.patterns;

import org.emoflon.neo.emsl.eMSL.ConditionOperator;

public class NeoAttributeExpression {
	
	private String varName;
	private String attrKey;
	private String attrValue;
	private ConditionOperator op;
	private String opString;
	
	public NeoAttributeExpression (String varName, String attrKey, String attrValue, ConditionOperator op) {
		
		this.varName = varName;
		this.attrKey = attrKey;
		this.attrValue = attrValue;
		this.op = op;
		this.opString = translateOpToString();
	}
	
	private String translateOpToString() {
		
		switch(op) {
		case EQ: return "="; 
		case GREATER: return ">"; 
		case GREATEREQ: return ">="; 
		case LESS: return "<";
		case LESSEQ: return "<="; 
		case NOTEQ: return "<>";
		case ASSIGN: return "=";
		default:
			throw new UnsupportedOperationException();
		}
	}

	public String getVarName() {
		return varName;
	}

	public String getAttrKey() {
		return attrKey;
	}

	public String getAttrValue() {
		return attrValue;
	}

	public ConditionOperator getOp() {
		return op;
	}
	
	public String getOpString() {
		return opString;
	}

}
