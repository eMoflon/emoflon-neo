package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.Operation
import org.emoflon.neo.emsl.eMSL.Action
import org.emoflon.neo.emsl.eMSL.Correspondence
import java.util.Map
import org.emoflon.neo.emsl.eMSL.Parameter
import java.util.Collection
import org.emoflon.neo.emsl.eMSL.ConditionOperator

class CO implements Operation {
	override String getNameExtension() {
		return "_CO"
	}

	override String getAction(Action action, boolean isSrc) {
		return ""
	}

	override String getTranslation(Action action, boolean isSrc) {
		return ""
	}

	override String compileCorrespondence(Correspondence corr) {
		'''
			-corr->«corr.target.name»
			{
				._type_ : "«corr.type.name»"
			}
		'''
	}
	
	override handleParameters(Map<Parameter, String> paramsToValues, Map<Parameter, String> paramsToProperty, Map<Parameter, Boolean> paramsToDomain, Map<String, Collection<Parameter>> paramGroups) {
		 for(group : paramGroups.values) {
		 	val representative = group.head
			for(param : group)
				paramsToValues.put(param, paramsToProperty.get(representative))
	 		paramsToValues.put(representative, null)
		 }
	}
	
	override getConditionOperator(ConditionOperator propOp, boolean isSrc) {
		if(propOp === ConditionOperator.ASSIGN)
			ConditionOperator.EQ.literal
		else
			propOp.literal
	}
}
