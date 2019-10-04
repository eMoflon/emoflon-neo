package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.Operation
import org.emoflon.neo.emsl.eMSL.Action
import org.emoflon.neo.emsl.eMSL.ActionOperator
import java.util.Map
import org.emoflon.neo.emsl.eMSL.Parameter
import java.util.Collection
import org.emoflon.neo.emsl.eMSL.ConditionOperator

class FWD implements Operation {
	override String getNameExtension() {
		return "_FWD"
	}

	override String getAction(Action pAction, boolean pIsSrc) {
		if(pIsSrc || pAction === null || !ActionOperator::CREATE.equals(pAction.getOp())) return "" else return "++"
	}

	override String getTranslation(Action pAction, boolean pIsSrc) {
		if(pIsSrc) if(pAction === null ||
			!ActionOperator::CREATE.equals(
				pAction.getOp())) return "~_tr_ : true" else return "~_tr_ : false\n~_tr_ := true" else return ""
	}
	
	override handleParameters(Map<Parameter, String> paramsToValues, Map<Parameter, String> paramsToProperty, Map<Parameter, Boolean> paramsToDomain, Map<String, Collection<Parameter>> paramGroups) {
		 for(group : paramGroups.values) {
		 	val representative = group.findFirst[param | paramsToDomain.get(param)]
		 	if(representative !== null) {
		 		for(param : group)
		 			paramsToValues.put(param, paramsToProperty.get(representative)) 
		 		paramsToValues.put(representative, null)
		 	}
		 }
	}
	
	override getConditionOperator(ConditionOperator propOp, boolean isSrc) {
		if(isSrc && propOp === ConditionOperator.ASSIGN)
			ConditionOperator.EQ.literal
		else
			propOp.literal
	}
}
