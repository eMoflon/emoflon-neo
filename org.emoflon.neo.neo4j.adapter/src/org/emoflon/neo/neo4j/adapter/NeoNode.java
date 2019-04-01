package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;

public class NeoNode {
	
	private String classType;
	private String varName;
	
	private Collection<NeoRelation> relations;
	
	public NeoNode (String classType, String varName) {
		this.classType = classType;
		this.varName = varName;
		this.relations = new ArrayList<>();
	}
	
	public String getClassType() {
		return classType;
	}
	
	public String getVarName() {
		return varName;
	}
	
	public Collection<NeoRelation> getRelations() {
		return relations;
	}
	
	public void addRelation (NeoRelation rel) {
		this.relations.add(rel);
	}
	
	@Override
	public String toString() {
		return varName + ":" + classType;
	}

}
