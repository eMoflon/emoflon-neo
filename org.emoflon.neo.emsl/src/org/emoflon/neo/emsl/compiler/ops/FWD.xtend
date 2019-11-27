package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.Operation
import org.emoflon.neo.emsl.eMSL.Action
import org.emoflon.neo.emsl.eMSL.ActionOperator
import java.util.Map
import org.emoflon.neo.emsl.eMSL.Parameter
import java.util.Collection
import org.emoflon.neo.emsl.eMSL.ConditionOperator
import org.emoflon.neo.emsl.eMSL.TripleRuleNAC
import org.emoflon.neo.emsl.compiler.TGGCompilerUtils.ParameterDomain
import org.emoflon.neo.emsl.compiler.ParameterData
import org.emoflon.neo.emsl.eMSL.SourceNAC

class FWD implements Operation {
	override String getNameExtension() {
		return "_FWD"
	}

	override String getAction(Action action, boolean isSrc) {
		if(isSrc || action === null || !ActionOperator::CREATE.equals(action.getOp())) return "" else return "++"
	}

	override String getTranslation(Action action, boolean isSrc) {
		if(isSrc) if(action === null ||
			!ActionOperator::CREATE.equals(
				action.getOp())) return "~_tr_ : true" else return "~_tr_ : false\n~_tr_ := true" else return ""
	}
	
	override handleParameters(Map<Parameter, ParameterData> paramsToData, Map<String, Collection<Parameter>> paramGroups) {
		 for(group : paramGroups.values) {
		 	val representative = paramsToData.get(group.findFirst[param | paramsToData.get(param).domain.equals(ParameterDomain.SRC)])
		 	if(representative !== null) {
		 		for(param : group)
		 			paramsToData.get(param).map(representative.containingBlock, representative.containingPropertyName)
		 		representative.map(null, null)
		 	}
		 }
	}
	
	override getConditionOperator(ConditionOperator propOp, boolean isSrc) {
		if(isSrc && propOp === ConditionOperator.ASSIGN)
			ConditionOperator.EQ.literal
		else
			propOp.literal
	}
	
	override preprocessNACs(Iterable<TripleRuleNAC> nacs) {
		return nacs.reject[it instanceof SourceNAC]
	}
	
	override requiresCorrModelCreation() {
		true
	}
}
