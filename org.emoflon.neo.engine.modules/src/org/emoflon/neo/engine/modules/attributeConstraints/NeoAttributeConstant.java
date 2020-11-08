package org.emoflon.neo.engine.modules.attributeConstraints;

import org.emoflon.neo.emsl.eMSL.AttributeExpression;
import org.emoflon.neo.emsl.eMSL.DataType;
import org.emoflon.neo.emsl.eMSL.EnumValue;
import org.emoflon.neo.emsl.eMSL.PrimitiveBoolean;
import org.emoflon.neo.emsl.eMSL.PrimitiveDouble;
import org.emoflon.neo.emsl.eMSL.PrimitiveInt;
import org.emoflon.neo.emsl.eMSL.PrimitiveString;
import org.emoflon.neo.emsl.eMSL.ValueExpression;
import org.neo4j.driver.Record;

public class NeoAttributeConstant extends NeoAttributeConstraintVariable {

	public NeoAttributeConstant(DataType type, ValueExpression expression, Record data) {
		super(type, "constant");
		bindToValue(extractRawValueFromExpression(expression, data));
	}

	private Object extractRawValueFromExpression(ValueExpression expression, Record data) {
		if(expression instanceof PrimitiveString) {
			var string = (PrimitiveString)expression;
			return string.getLiteral();
		}
		
		if(expression instanceof PrimitiveInt) {
			var _int = (PrimitiveInt)expression;
			return _int.getLiteral();
		}
		
		if(expression instanceof PrimitiveDouble) {
			var _double = (PrimitiveDouble)expression;
			return _double.getLiteral();
		}
		
		if(expression instanceof PrimitiveInt) {
			var _int = (PrimitiveInt)expression;
			return _int.getLiteral();
		}
		
		if(expression instanceof PrimitiveBoolean) {
			var _bool = (PrimitiveBoolean)expression;
			return _bool.isTrue();
		}
		
		if(expression instanceof EnumValue) {
			var enumValue = (EnumValue)expression;
			return enumValue.getLiteral();
		}
		
		// Handle attribute expressions by extracting from match
		if(expression instanceof AttributeExpression) {
			var attrExpr = (AttributeExpression)expression;
			var extractedValue = data.get(attrExpr.getNode().getName()).get(attrExpr.getTarget().getAttribute().getName());
			return extractedValue.asObject();
		}
		
		throw new IllegalArgumentException("Unhandled type of expression: " + expression.getClass().getName());
	}
}
