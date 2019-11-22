package org.emoflon.neo.emsl.compiler

import org.emoflon.neo.emsl.eMSL.Action
import org.emoflon.neo.emsl.eMSL.ConditionOperator
import java.util.Map
import org.emoflon.neo.emsl.eMSL.Parameter
import java.util.Collection
import org.emoflon.neo.emsl.eMSL.TripleRuleNAC
import java.util.Collections

abstract class ILPOperation implements Operation {
	
	override getTranslation(Action action, boolean isSrc) {
		return ""
	}
	
	override getConditionOperator(ConditionOperator propOp, boolean isSrc) {
		if(propOp === ConditionOperator.ASSIGN)
			ConditionOperator.EQ.literal
		else
			propOp.literal
	}
	
	override handleParameters(Map<Parameter, ParameterData> paramsToData, Map<String, Collection<Parameter>> paramGroups) {
		for(group : paramGroups.values) {
			val representative = paramsToData.get(selectParamGroupRepresentative(group, paramsToData))
			for(param : group)
				paramsToData.get(param).map(representative.containingBlock, representative.containingPropertyName)
	 		representative.map(null, null)
		 }
	}
	
	def Parameter selectParamGroupRepresentative(Collection<Parameter> paramGroup, Map<Parameter, ParameterData> paramsToData)
	
	override Iterable<TripleRuleNAC> preprocessNACs(Iterable<TripleRuleNAC> nacs) {
		return Collections.emptyList
	}
}