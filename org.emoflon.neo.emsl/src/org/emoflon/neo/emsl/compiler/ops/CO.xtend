package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.Operation
import org.emoflon.neo.emsl.eMSL.Action
import org.emoflon.neo.emsl.eMSL.Correspondence
import java.util.Map
import org.emoflon.neo.emsl.eMSL.Parameter
import java.util.Collection
import org.emoflon.neo.emsl.eMSL.ConditionOperator
import org.emoflon.neo.emsl.compiler.ParameterData
import org.emoflon.neo.emsl.eMSL.TripleRuleNAC
import java.util.Collections

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
	
	override handleParameters(Map<Parameter, ParameterData> paramsToData, Map<String, Collection<Parameter>> paramGroups) {
		 for(group : paramGroups.values) {
			val representative = paramsToData.get(group.head)
			for(param : group)
				paramsToData.get(param).map(representative.containingBlock, representative.containingPropertyName)
	 		representative.map(null, null)
		 }
	}
	
	override getConditionOperator(ConditionOperator propOp, boolean isSrc) {
		if(propOp === ConditionOperator.ASSIGN)
			ConditionOperator.EQ.literal
		else
			propOp.literal
	}

	override Iterable<TripleRuleNAC> preprocessNACs(Iterable<TripleRuleNAC> nacs) {
		return Collections.emptyList
	}
}
