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
	
	private int countRel = 0;
	private int countProp = 0;
	private int countCond = 0;
	
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
	
	public int getCountRelatiion() {
		return countRel;
	}
	public int getCountProperties() {
		return countProp;
	}
	public int getCountConditions() {
		return countCond;
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
		countRel++;
		this.relations.add(new NeoRelation(relType, relName, toNodeType, toVarName));
	}
	
	public void addProperty(String name, String value) {
		countProp++;
		this.propteries.add(new NeoProperty(name, value, varName));
	}
	
	public void addCondition(String name, NodeBlockConditionOperator op, String value) {
		countCond++;
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
