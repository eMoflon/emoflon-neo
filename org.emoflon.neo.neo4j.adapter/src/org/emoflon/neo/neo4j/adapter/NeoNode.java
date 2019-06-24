package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;

public class NeoNode {
	private String classType;
	private String varName;

	private Collection<NeoProperty> properties;
	private Collection<NeoRelation> relations;
	private Collection<NeoRelation> conditions;

	public NeoNode(String classType, String varName) {
		this.classType = classType;
		this.varName = varName;
		this.properties = new ArrayList<>();
		this.relations = new ArrayList<>();
		this.conditions = new ArrayList<>();
	}

	public String getClassType() {
		return classType;
	}

	public String getVarName() {
		return varName;
	}

	public Collection<NeoProperty> getProperties() {
		return properties;
	}

	public Collection<NeoRelation> getRelations() {
		return relations;
	}

	public Collection<NeoRelation> getConditions() {
		return conditions;
	}

	public void addProperty(String name, String value) {
		this.properties.add(new NeoProperty(name, value));
	}

	public void addRelation(NeoRelation rel) {
		this.relations.add(rel);
	}

	public void addCondition(NeoRelation rel) {
		this.conditions.add(rel);
	}

	@Override
	public String toString() {
		return CypherPatternBuilder.cypherNode(varName, classType, properties);
	}
}
