package org.emoflon.neo.engine.modules.attributeConstraints;

import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.emsl.eMSL.DataType;
import org.emoflon.neo.emsl.eMSL.PrimitiveString;
import org.emoflon.neo.emsl.eMSL.ValueExpression;

public class NeoAttributeConstant extends NeoAttributeConstraintVariable {

	public NeoAttributeConstant(DataType type, ValueExpression expression, NeoMatch match) {
		super(type, "constant");
		bindToValue(extractRawValueFromExpression(expression, match));
	}

	private Object extractRawValueFromExpression(ValueExpression expression, NeoMatch match) {
		if(expression instanceof PrimitiveString) {
			var string = (PrimitiveString)expression;
			return string.getLiteral();
		}
		
		//FIXME: Handle other literal values
		//FIXME: Handle attribute expressions by extracting from match
		
		throw new IllegalArgumentException("Unhandled type of expression: " + expression.getClass().getName());
	}
}
