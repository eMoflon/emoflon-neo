package org.emoflon.neo.cypher.common;

import static org.emoflon.neo.cypher.models.NeoCoreBuilder.computeLabelsFromType;

import java.util.ArrayList;
import java.util.List;

import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;

public class NeoNode extends NeoElement {
	private List<String> labels;

	public NeoNode(String name, List<String> labels, List<ModelPropertyStatement> properties) {
		super(name, labels.get(0), properties);
		this.labels = new ArrayList<>(labels);
	}

	public NeoNode(ModelNodeBlock nb) {
		this(nb.getName(), computeLabelsFromType(nb.getType()), nb.getProperties());
	}

	public List<String> getLabels() {
		return labels;
	}

	public void setName(String name) {
		this.name = name;
	}
}
