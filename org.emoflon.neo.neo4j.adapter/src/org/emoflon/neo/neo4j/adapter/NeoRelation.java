package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;

/*
 * Class for representing an EMSL relation for creating Cypher queries
 */
public class NeoRelation {
	private String relType;
	private String toNodeVar;
	private String toNodeLabel;
	private String varName;
	private Collection<NeoProperty> properties;

	/*
	 * @param from the source NeoNode node of the relation
	 * @param index the index of the relation per source node
	 * @param relType the label/class of the relation 
	 * @param props the properties of the relation
	 * @param toNodelLabel the label/class of the target node
	 * @param toNodeVar the variable used in cypher of the target node
	 */
	public NeoRelation(NeoNode from, String varName, String relType, List<ModelPropertyStatement> props, String toNodeLabel, String toNodeVar) {
		this.relType = relType;
		this.toNodeVar = toNodeVar;
		this.toNodeLabel = toNodeLabel;
		this.varName = varName;

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
