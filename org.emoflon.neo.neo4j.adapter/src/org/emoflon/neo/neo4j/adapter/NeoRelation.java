package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;
import org.emoflon.neo.emsl.util.EMSLUtil;

/**
 * Class for representing an EMSL relation for creating Cypher queries
 * 
 * @author Jannik Hinz
 *
 */
public class NeoRelation {
	private String relType;
	private String toNodeVar;
	private String toNodeLabel;
	private String varName;
	private Collection<NeoProperty> properties;

	/**
	 * @param from         the source NeoNode node of the relation
	 * @param index        the index of the relation per source node
	 * @param relType      the label/class of the relation
	 * @param props        the properties of the relation
	 * @param toNodelLabel the label/class of the target node
	 * @param toNodeVar    the variable used in cypher of the target node
	 */
	public NeoRelation(NeoNode from, String varName, String relType, List<ModelPropertyStatement> props,
			String toNodeLabel, String toNodeVar) {
		this.relType = relType;
		this.toNodeVar = toNodeVar;
		this.toNodeLabel = toNodeLabel;
		this.varName = varName;

		properties = new ArrayList<>();
		props.forEach(prop -> addProperty(prop.getType().getName(), EMSLUtil.handleValue(prop.getValue())));
	}

	/**
	 * Adds property to the relation by adding a NeoProperty to the props list
	 * 
	 * @param name  or key of the attribute/property
	 * @param value value of the attribute/property
	 */
	public void addProperty(String name, String value) {
		properties.add(new NeoProperty(name, value));
	}

	/**
	 * Return the Relation Type (the Label)
	 * 
	 * @return type of the relation (label)
	 */
	public String getRelType() {
		return relType;
	}

	/**
	 * Return the variable name of the target node of the relation
	 * 
	 * @return variable name of the target node of the relation
	 */
	public String getToNodeVar() {
		return toNodeVar;
	}

	/**
	 * Return the Label /Type of the target node of the relation
	 * 
	 * @return Label /Type of the target node of the relation
	 */
	public String getToNodeLabel() {
		return toNodeLabel;
	}

	/**
	 * Return the list of all properties of the relation
	 * 
	 * @return NeoProperty list of properties of the relation
	 */
	public Collection<NeoProperty> getProperties() {
		return properties;
	}

	/**
	 * Return the variable name of the source node
	 * 
	 * @return variable name of the source node
	 */
	public String getVarName() {
		return varName;
	}
}
