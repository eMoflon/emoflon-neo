package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;

public class NeoNode {

	private String classType;
	private String varName;

	private Collection<NeoProperty> propteries;

	public NeoNode(String classType, String varName) {
		this.classType = classType;
		this.varName = varName;
		this.propteries = new ArrayList<>();

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

	public void addProperty(String name, String value) {
		this.propteries.add(new NeoProperty(name, value, varName));
	}

	@Override
	public String toString() {
		return CypherPatternBuilder.cypherNode(varName, classType, propteries);
	}

}
