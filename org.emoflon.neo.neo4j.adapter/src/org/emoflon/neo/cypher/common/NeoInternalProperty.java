package org.emoflon.neo.cypher.common;

import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;

public class NeoInternalProperty extends NeoProperty {
	private String inferredType;

	public NeoInternalProperty(ModelPropertyStatement prop, NeoElement element) {
		super(prop, element);
		inferredType = prop.getInferredType();
	}

	public String getName() {
		return inferredType;
	}
}
