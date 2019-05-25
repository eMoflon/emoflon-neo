package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;

import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;

public class NeoRelation {

	private String relType;
	private String relName;
	private NeoNode fromNode;
	private String toNode;
	private String toVarName;

	private Collection<NeoProperty> properties;

	public NeoRelation(String relType, Collection<ModelPropertyStatement> props, NeoNode fromNode, String toNode,
			String toVarName) {

		this.properties = new ArrayList<>();

		this.relType = relType;
		this.relName = "";
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.toVarName = toVarName;

		props.forEach(prop -> addProperty(prop.getType().getName(), NeoUtil.handleValue(prop.getValue())));
	}

	public NeoRelation(String relType, NeoNode fromNode, String toNode, String toVarName) {

		this.properties = new ArrayList<>();

		this.relType = relType;
		this.relName = "";
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.toVarName = toVarName;
	}

	public String getRelType() {
		return relType;
	}

	public String getToNode() {
		return toNode;
	}

	public String getToVarName() {
		return toVarName;
	}

	public String getRelName() {
		return relName;
	}

	public Collection<NeoProperty> getProperties() {
		return properties;
	}

	public void addProperty(String name, String value) {
		this.properties.add(new NeoProperty(name, value, toVarName));
	}

	@Override
	public String toString() {
		return CypherPatternBuilder.cypherRelation(fromNode, this, properties);
	}

}
