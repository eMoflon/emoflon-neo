package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.emf.common.util.EList;
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;

public class NeoNode {

	private String classType;
	private String varName;

	private Collection<NeoProperty> propteries;
	private Collection<NeoRelation> relations;

	public NeoNode(String classType, String varName) {
		this.classType = classType;
		this.varName = varName;
		this.propteries = new ArrayList<>();
		this.relations = new ArrayList<>();
	}

	public String getClassType() {
		return classType;
	}

	public String getVarName() {
		return varName;
	}

	public Collection<NeoProperty> getProperties() {
		return propteries;
	}
	public Collection<NeoRelation> getRelations() {
		return relations;
	}

	public void addProperty(String name, String value) {
		this.propteries.add(new NeoProperty(name, value));
	}
	public void addRelation(NeoRelation rel) {
		this.relations.add(rel);
	}


	@Override
	public String toString() {
		return CypherPatternBuilder.cypherNode(varName, classType, propteries);
	}




}
