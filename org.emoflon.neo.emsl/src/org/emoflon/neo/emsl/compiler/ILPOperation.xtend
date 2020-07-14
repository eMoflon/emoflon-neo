package org.emoflon.neo.emsl.compiler

import org.emoflon.neo.emsl.eMSL.Action
import org.emoflon.neo.emsl.eMSL.ConditionOperator
import java.util.Map
import org.emoflon.neo.emsl.eMSL.Parameter
import java.util.Collection
import org.emoflon.neo.emsl.eMSL.TripleRuleNAC
import java.util.Collections

abstract class ILPOperation implements Operation {

	override getTranslation(Action action, Domain domain) {
		return ""
	}

	override getConditionOperator(ConditionOperator propOp, Domain domain) {
		if (propOp === ConditionOperator.ASSIGN)
			ConditionOperator.EQ.literal
		else
			propOp.literal
	}

	override handleParameters(Map<Parameter, ParameterData> paramsToData,
		Map<String, Collection<Parameter>> paramGroups) {

		for (group : paramGroups.values) {
			val representative = paramsToData.get(selectParamGroupRepresentative(group, paramsToData))
			if (representative !== null) {
				for (param : group)
					paramsToData.get(param).map(representative.containingBlock, representative.containingPropertyName)
				representative.unmap()
			}
		}
	}
	
	override String additionalFields(String tggName) {
		return additionalFields(tggName, "Gurobi")
	}
	
	def String additionalFields(String tggName, String solver)
	
	def Parameter selectParamGroupRepresentative(Collection<Parameter> paramGroup, Map<Parameter, ParameterData> paramsToData) 
	
	override Iterable<TripleRuleNAC> preprocessNACs(Iterable<TripleRuleNAC> nacs) {
		return Collections.emptyList
	}
}
