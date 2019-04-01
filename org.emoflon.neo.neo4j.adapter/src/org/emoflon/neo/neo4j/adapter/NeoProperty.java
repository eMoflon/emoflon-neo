package org.emoflon.neo.neo4j.adapter;

import org.emoflon.neo.emsl.eMSL.NodeBlockConditionOperator;

public class NeoProperty {
	
	private String name;
	private String value;
	
	private String classVarName;

	public NeoProperty(String name, String value, String classVarName) {
		
		this.classVarName = classVarName;
		
		this.name = name;
		this.value = value;
		
	}
	
	@Override
	public String toString() {
		String result = "";
		return result + " " + classVarName + "." + name + ":" + value;
	}

}
