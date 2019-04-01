package org.emoflon.neo.neo4j.adapter;

public class NeoRelation {
	
	private String relType;
	private String relName;
	
	public NeoRelation(String relType, String relName) {
		this.relType = relType;
		this.relName = relName;
	}

	public String getRelType() {
		return relType;
	}

	public String getRelName() {
		return relName;
	}

}
