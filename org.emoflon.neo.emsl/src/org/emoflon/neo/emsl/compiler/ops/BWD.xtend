package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.Operation
import org.emoflon.neo.emsl.eMSL.Action
import org.emoflon.neo.emsl.eMSL.ActionOperator
import java.util.Map
import org.emoflon.neo.emsl.eMSL.Parameter
import java.util.Collection
import org.emoflon.neo.emsl.eMSL.ConditionOperator
import org.emoflon.neo.emsl.compiler.TGGCompilerUtils.ParameterDomain

class BWD implements Operation {
	override String getNameExtension() {
		return "_BWD"
	}

	override String getAction(Action action, boolean isSrc) {
		if(!isSrc || action === null || !ActionOperator::CREATE.equals(action.getOp())) return "" else return "++"
	}

	override String getTranslation(Action pAction, boolean isSrc) {
		if(!isSrc) if(pAction === null ||
			!ActionOperator::CREATE.equals(
				pAction.getOp())) return "~_tr_ : true" else return "~_tr_ : false\n~_tr_ := true" else return ""
	}
	
	override handleParameters(Map<Parameter, String> paramsToValue, Map<Parameter, String> paramsToContainingProperty, Map<Parameter, ParameterDomain> paramsToDomain, Map<String, Collection<Parameter>> paramGroups) {
		 for(group : paramGroups.values) {
		 	val representative = group.findFirst[param | paramsToDomain.get(param).equals(ParameterDomain.TRG)]
		 	if(representative !== null) {
		 		for(param : group)
		 			paramsToValue.put(param, paramsToContainingProperty.get(representative)) 
		 		paramsToValue.put(representative, null)
		 	}
		 }
	}
	
	override getConditionOperator(ConditionOperator propOp, boolean isSrc) {
		if(!isSrc && propOp === ConditionOperator.ASSIGN)
			ConditionOperator.EQ.literal
		else
			propOp.literal
	}
}
