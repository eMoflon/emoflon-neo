package org.emoflon.neo.neo4j.adapter.common;

/**
 * Class for representing an EMSL property of nodes or relations for creating
 * Cypher queries
 * 
 * @author Jannik Hinz
 *
 */
public class NeoProperty {
	private String name;
	private String value;

	/**
	 * 
	 * @param name  or key of the attribute/property
	 * @param value value of the attribute/property
	 */
	public NeoProperty(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Returns the name of the attribute property
	 * 
	 * @return name of the attribute property
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the value of the attribute property
	 * 
	 * @return value of the attribute property
	 */
	public String getValue() {
		return value;
	}

}
