package org.emoflon.neo.neo4j.adapter;

public class NeoRelation {
	
	private String relType;
	private String relName;
	private NeoNode fromNode;
	private String toNode;
	private String toVarName;
	
	public NeoRelation(String relType, String relName, NeoNode fromNode, String toNode, String toVarName) {
		this.relType = relType;
		this.relName = relName;
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.toVarName = toVarName;
	}

	public String getRelType() {
		return relType;
	}

	public String getToNode() {
		return toNode;
	}

	public String getToVarName() {
		return toVarName;
	}

	public String getRelName() {
		return relName;
	}
	
	@Override
	public String toString() {
		return CypherPatternBuilder.cypherRelation(fromNode, this);
	}

}
