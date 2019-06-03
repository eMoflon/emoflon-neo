package org.emoflon.neo.neo4j.adapter;

public class NeoProperty {
	private String name;
	private String value;

	public NeoProperty(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return CypherPatternBuilder.cypherProperty(name, value);
	}

}
