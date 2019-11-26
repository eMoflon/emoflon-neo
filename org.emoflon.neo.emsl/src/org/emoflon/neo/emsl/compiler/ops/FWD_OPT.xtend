package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.ILPOperation
import org.emoflon.neo.emsl.eMSL.Action
import org.emoflon.neo.emsl.eMSL.ActionOperator
import java.util.Collection
import org.emoflon.neo.emsl.eMSL.Parameter
import org.emoflon.neo.emsl.compiler.TGGCompilerUtils.ParameterDomain
import org.emoflon.neo.emsl.compiler.ParameterData
import java.util.Map
import org.emoflon.neo.emsl.eMSL.ConditionOperator

class FWD_OPT extends ILPOperation {
	override String getNameExtension() {
		return "_FWD_OPT"
	}
	
	override String getAction(Action action, boolean isSrc) {
		if(isSrc || action === null || !ActionOperator::CREATE.equals(action.getOp())) return "" else return "++"
	}
	
	override getConditionOperator(ConditionOperator propOp, boolean isSrc) {
		if(isSrc)
			super.getConditionOperator(propOp, isSrc)
		else
			propOp.literal
	}
	
	override requiresCorrModelCreation() {
		true
	}
	
	override selectParamGroupRepresentative(Collection<Parameter> paramGroup, Map<Parameter, ParameterData> paramsToData) {
		paramGroup.findFirst[param | paramsToData.get(param).domain.equals(ParameterDomain.SRC)]
	}
}