package org.emoflon.neo.neo4j.adapter;

public class NeoRelation {
	
	private String relType;
	private String relName;
	private String toNodeType;
	private String toVarName;
	
	public NeoRelation(String relType, String relName, String toNodeType, String toVarName) {
		this.relType = relType;
		this.relName = relName;
		this.toNodeType = toNodeType;
		this.toVarName = toVarName;
	}

	public String getRelType() {
		return relType;
	}

	public String getToNodeType() {
		return toNodeType;
	}

	public String getToVarName() {
		return toVarName;
	}

	public String getRelName() {
		return relName;
	}
	
	@Override
	public String toString() {
		// complete
		// return "-[" + relName + ":" + relType + "]->(" + toVarName + ":" + toNodeType + ")";
		
		// without class types for use in WHERE clause
		return "-[" + relName + ":" + relType + "]->(" + toVarName + ")";
	}

}
