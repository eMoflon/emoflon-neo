package org.emoflon.neo.neo4j.adapter.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;

public class NeoNode {
	private Collection<String> labels;
	private String varName;

	private Collection<NeoProperty> properties;
	private Collection<NeoRelation> relations;

	public NeoNode(Collection<String> labels, String varName) {
		this.labels = new ArrayList<>(labels);
		this.varName = varName;
		this.properties = new ArrayList<>();
		this.relations = new ArrayList<>();
	}

	public Collection<String> getLabels() {
		return labels;
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

	public void addProperty(String name, String value) {
		this.properties.add(new NeoProperty(name, value));
	}

	public void addRelation(String varName, List<String> relTypes, String lower, String upper,
			List<ModelPropertyStatement> props, ModelNodeBlock target, String toVarName) {
		this.relations.add(new NeoRelation(this, varName, relTypes, lower, upper, props, target, toVarName));
	}

	public void addRelation(NeoRelation rel) {
		this.relations.add(rel);
	}
}
