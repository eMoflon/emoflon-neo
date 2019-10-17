package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.Operation
import org.emoflon.neo.emsl.eMSL.Action
import org.emoflon.neo.emsl.eMSL.Correspondence
import java.util.Map
import org.emoflon.neo.emsl.eMSL.Parameter
import java.util.Collection
import org.emoflon.neo.emsl.eMSL.ConditionOperator
import org.emoflon.neo.emsl.compiler.TGGCompilerUtils.ParameterDomain

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
	
	override handleParameters(Map<Parameter, String> paramsToValue, Map<Parameter, String> paramsToContainingProperty, Map<Parameter, ParameterDomain> paramsToDomain, Map<String, Collection<Parameter>> paramGroups) {
		 for(group : paramGroups.values) {
		 	val representative = group.head
			for(param : group)
				paramsToValue.put(param, paramsToContainingProperty.get(representative))
	 		paramsToValue.put(representative, null)
		 }
	}
	
	override getConditionOperator(ConditionOperator propOp, boolean isSrc) {
		if(propOp === ConditionOperator.ASSIGN)
			ConditionOperator.EQ.literal
		else
			propOp.literal
	}
}
