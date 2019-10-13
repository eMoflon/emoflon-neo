package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.Operation
import org.emoflon.neo.emsl.eMSL.Action
import org.emoflon.neo.emsl.eMSL.ActionOperator
import java.util.Map
import org.emoflon.neo.emsl.eMSL.Parameter
import java.util.Collection
import org.emoflon.neo.emsl.eMSL.ConditionOperator
import org.emoflon.neo.emsl.eMSL.AtomicPattern
import java.util.HashSet
import org.emoflon.neo.emsl.eMSL.TripleRuleNAC

class MODELGEN implements Operation {
	override String getNameExtension() {
		return "_GEN"
	}

	override String getAction(Action action, boolean isSrc) {
		if(action === null || !ActionOperator::CREATE.equals(action.getOp())) return "" else return "++"
	}

	override String getTranslation(Action action, boolean isSrc) {
		return ""
	}
	
	override handleParameters(Map<Parameter, String> paramsToValues, Map<Parameter, String> paramsToProperty, Map<Parameter, Boolean> paramsToDomain, Map<String, Collection<Parameter>> paramGroups) {
		// Nothing to do here, all parameters are already mapped to their correct String representation
	}
	
	override getConditionOperator(ConditionOperator propOp, boolean isSrc) {
		propOp.literal
	}

	override Collection<AtomicPattern> compileNACs(Collection<TripleRuleNAC> nacs) {
		val nacPatterns = new HashSet
		nacs.forEach[nacPatterns.add(it.pattern)]
		return nacPatterns
	}
}
