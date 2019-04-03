package org.emoflon.neo.neo4j.adapter;

public class NeoProperty {

	private String name;
	private String value;

	private String classVarName;

	public NeoProperty(String name, String value, String classVarName) {

		this.classVarName = classVarName;
		this.name = name;
		this.value = value;

	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public String getClassVarName() {
		return classVarName;
	}

	@Override
	public String toString() {
		return CypherPatternBuilder.cypherProperty(name, value, classVarName);
	}

}
