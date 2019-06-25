package org.emoflon.neo.neo4j.adapter;

public class NeoCondition {
	
	private NeoConstraint c;
	private NeoPattern p;
	
	private String name;

	public NeoCondition(NeoConstraint c, NeoPattern p, String name) {
		
		this.c = c;
		this.p = p;
		
		this.name = name;
	}
	
	public boolean isSatisfied() {
		return false;
	}

}
