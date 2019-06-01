package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.emf.common.util.EList;
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;

public class NeoRelation {

	private String relType;
	private String relVarName;
	private String toNodeVar;
	private String toNodeLabel;

	private Collection<NeoProperty> properties;

	public NeoRelation(String relType, EList<ModelPropertyStatement> props, String toNodeLabel, String toNodeVar) {

		this.properties = new ArrayList<>();

		this.relType = relType;
		this.relVarName = "";
		this.toNodeLabel = toNodeLabel;
		this.toNodeVar = toNodeVar;

		props.forEach(prop -> addProperty(prop.getType().getName(), NeoUtil.handleValue(prop.getValue())));
	}

	public void addProperty(String name, String value) {
		this.properties.add(new NeoProperty(name, value));
	}

	@Override
	public String toString() {
		//return CypherPatternBuilder.cypherRelation(fromNode, this, properties);
		return "";
	}

	public String getRelType() {
		return relType;
	}

	public String getRelVarName() {
		return relVarName;
	}

	public String getToNodeVar() {
		return toNodeVar;
	}

	public String getToNodeLabel() {
		return toNodeLabel;
	}

	public Collection<NeoProperty> getProperties() {
		return properties;
	}

}
