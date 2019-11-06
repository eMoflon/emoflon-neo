package org.emoflon.neo.cypher.common;

import org.emoflon.neo.emsl.eMSL.MetamodelPropertyStatement;
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
	private NeoElement element;
	private MetamodelPropertyStatement type;
	private String value;

	public NeoProperty(NeoElement element, MetamodelPropertyStatement type, String value) {
		this.element = element;
		this.type = type;
		this.value = value;
	}

	public NeoProperty(ModelPropertyStatement prop, NeoElement element) {
		this(element, prop.getType(), EMSLUtil.handleValue(prop.getValue()));
	}

	public String getElement() {
		return element.getName();
	}
	
	/**
	 * Returns the name of the attribute property
	 * 
	 * @return name of the attribute property
	 */
	public String getName() {
		return type.getName();
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
