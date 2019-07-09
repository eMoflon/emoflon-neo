package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;

/**
 * Class for representing an EMSL node for creating Cypher queries
 * 
 * @author Jannik Hinz
 *
 */
public class NeoNode {
	private String classType;
	private String varName;

	private Collection<NeoProperty> properties;
	private Collection<NeoRelation> relations;

	/**
	 * @param classType the label/class of the node
	 * @param varName   the variable used later in Cypher
	 */
	public NeoNode(String classType, String varName) {
		this.classType = classType;
		this.varName = varName;
		this.properties = new ArrayList<>();
		this.relations = new ArrayList<>();
	}

	/**
	 * Return the ClassType (the Label) of the node
	 * 
	 * @return ClassType (the Label) of the node
	 */
	public String getClassType() {
		return classType;
	}

	/**
	 * Returns the variable name used for identifying the node EMSL and later Cypher
	 * 
	 * @return variable name of the node
	 */
	public String getVarName() {
		return varName;
	}

	/**
	 * Return a list of all Properties of the given node
	 * 
	 * @return NeoProperties list of all Properties of the given node
	 */
	public Collection<NeoProperty> getProperties() {
		return properties;
	}

	/**
	 * Return a list of all Relations of the given node
	 * 
	 * @return NeoRelation list of all Relations of the given node
	 */
	public Collection<NeoRelation> getRelations() {
		return relations;
	}

	/**
	 * Add a new property to the node by creating a NeoProperty object and adding it
	 * to the list
	 * 
	 * @param name  of the attribute property
	 * @param value of the attribute property
	 */
	public void addProperty(String name, String value) {
		this.properties.add(new NeoProperty(name, value));
	}

	// TODO [Jannik] Change params such that the relation is created here
	public void addRelation(String varName, String relType, List<ModelPropertyStatement> props, String toNodeLabel, String toNodeVar) {
		this.relations.add(new NeoRelation(this, varName, relType, props, toNodeLabel, toNodeVar));
	}

	@Override
	public String toString() {
		return CypherPatternBuilder.cypherNode(varName, classType, properties);
	}
}
