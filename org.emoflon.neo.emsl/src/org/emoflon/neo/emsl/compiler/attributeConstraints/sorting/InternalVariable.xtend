package org.emoflon.neo.emsl.compiler.attributeConstraints.sorting

import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend.lib.annotations.EqualsHashCode
import org.emoflon.neo.emsl.eMSL.ValueExpression

@EqualsHashCode
class InternalVariable {
	@Accessors
	transient ValueExpression value
	String representation
	
	new(ValueExpression value, String representation) {
		this.value = value
		this.representation = representation
	}
	
}