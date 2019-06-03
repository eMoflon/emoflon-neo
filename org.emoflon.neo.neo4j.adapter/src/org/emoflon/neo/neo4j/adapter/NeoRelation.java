package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;

public class NeoRelation {
	private String relType;
	private String toNodeVar;
	private String toNodeLabel;
	private String varName;
	private Collection<NeoProperty> properties;

	public NeoRelation(NeoNode from, int index, String relType, List<ModelPropertyStatement> props, String toNodeLabel,
			String toNodeVar) {
		this.relType = relType;
		this.toNodeVar = toNodeVar;
		this.toNodeLabel = toNodeLabel;
		this.varName = from.getVarName() + "_" + relType + "_" + index + "_" + toNodeVar;

		properties = new ArrayList<>();
		props.forEach(prop -> addProperty(prop.getType().getName(), NeoUtil.handleValue(prop.getValue())));
	}

	public void addProperty(String name, String value) {
		properties.add(new NeoProperty(name, value));
	}

	public String getRelType() {
		return relType;
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

	public String getVarName() {
		return varName;
	}
}
