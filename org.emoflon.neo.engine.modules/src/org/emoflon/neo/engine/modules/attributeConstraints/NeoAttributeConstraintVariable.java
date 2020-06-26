package org.emoflon.neo.engine.modules.attributeConstraints;

import org.emoflon.neo.emsl.eMSL.DataType;

public class NeoAttributeConstraintVariable {
	private Object value;
	private DataType type;
	private boolean bound;
	private String name;
	
	public NeoAttributeConstraintVariable(DataType type, String name) {
		this.type = type;
		this.bound = false;
		this.name = name;
	}
	
	public Object getValue() {
		return value;
	}
	
	public String getName() {
		return name;
	}

	public void bindToValue(Object value) {
		this.value = value;
		bound = true;
	}

	public DataType getType() {
		return type;
	}

	public boolean isBound() {
		return bound;
	}
	
	@Override
	public String toString() {
		return getName() + " -> " + getValue();
	}
}
