package org.emoflon.neo.cypher.common;

import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;
import org.emoflon.neo.emsl.util.EMSLUtil;

/**
 * Class for representing an EMSL property of nodes or relations for creating
 * Cypher queries
 * 
 * @author Jannik Hinz
 *
 */
public class NeoProperty {
	private String element;
	private String name;
	private String value;

	/**
	 * 
	 * @param name  or key of the attribute/property
	 * @param value value of the attribute/property
	 */
	public NeoProperty(String element, String name, String value) {
		this.name = name;
		this.value = value;
		this.element = element;
	}

	public NeoProperty(ModelPropertyStatement prop, NeoElement element) {
		this(element.getName(), prop.getType().getName(), EMSLUtil.handleValue(prop.getValue()));
	}

	public String getElement() {
		return element;
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
