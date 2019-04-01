package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;

import org.emoflon.neo.emsl.eMSL.NodeBlockConditionOperator;

public class NeoNode {
	
	private String classType;
	private String varName;
	
	private Collection<NeoRelation> relations;
	private Collection<NeoProperty> propteries;
	private Collection<NeoCondition> conditions;
	
	public NeoNode (String classType, String varName) {
		this.classType = classType;
		this.varName = varName;
		this.relations = new ArrayList<>();
		this.propteries = new ArrayList<>();
		this.conditions = new ArrayList<>();
		
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
	public Collection<NeoProperty> getProperties() {
		return propteries;
	}
	public Collection<NeoCondition> getConditions() {
		return conditions;
	}
	
	public void addRelation (String relType, String relName, String toNodeType, String toVarName) {
		this.relations.add(new NeoRelation(relType, relName, toNodeType, toVarName));
	}
	
	public void addProperty(String name, String value) {
		this.propteries.add(new NeoProperty(name, value, varName));
	}
	
	public void addCondition(String name, NodeBlockConditionOperator op, String value) {
		this.conditions.add(new NeoCondition(name, op, value, varName));
	}
	
	@Override
	public String toString() {
		return "(" + varName + ":" + classType + ")";
	}
	
	public String toStringWithoutClassType() {
		return "(" + varName + ")";
	}

}
