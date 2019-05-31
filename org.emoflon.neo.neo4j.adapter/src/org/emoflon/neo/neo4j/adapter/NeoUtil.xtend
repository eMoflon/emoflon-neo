package org.emoflon.neo.neo4j.adapter

import java.util.Collection
import org.emoflon.neo.emsl.eMSL.EnumValue
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock
import org.emoflon.neo.emsl.eMSL.MetamodelPropertyStatement
import org.emoflon.neo.emsl.eMSL.PrimitiveBoolean
import org.emoflon.neo.emsl.eMSL.PrimitiveInt
import org.emoflon.neo.emsl.eMSL.PrimitiveString
import org.emoflon.neo.emsl.eMSL.Value
import org.emoflon.neo.emsl.util.EMSLUtil

class NeoUtil {
	def static String handleValue(Value value) {
		if(value instanceof PrimitiveString) return PrimitiveString.cast(value).getLiteral()

		// TODO[Jannik] Is this the best way of handling ints, bools, and enum literals?
		if(value instanceof PrimitiveInt) return Integer.toString(PrimitiveInt.cast(value).getLiteral())

		if(value instanceof PrimitiveBoolean) return Boolean.toString(PrimitiveBoolean.cast(value).isTrue())

		if(value instanceof EnumValue) return "\""+EnumValue.cast(value).getLiteral().getName().toString()+"\""

		// TODO[Jannik] How to handle attribute expressions?
		throw new IllegalArgumentException('''Not yet able to handle: «value»''')
	}
	
	def static Collection<MetamodelPropertyStatement> allPropertiesOf(MetamodelNodeBlock type){
		EMSLUtil.thisAndAllSuperTypes(type).flatMap[t|t.properties].toSet
	}
}
