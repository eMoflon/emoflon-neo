package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.Operation
import org.emoflon.neo.emsl.eMSL.Action
import org.emoflon.neo.emsl.eMSL.ActionOperator
import java.util.Map
import org.emoflon.neo.emsl.eMSL.Parameter
import java.util.Collection
import org.emoflon.neo.emsl.eMSL.ConditionOperator
import org.emoflon.neo.emsl.compiler.TGGCompilerUtils.ParameterDomain
import org.emoflon.neo.emsl.compiler.ParameterData
import org.emoflon.neo.emsl.eMSL.TripleRuleNAC
import org.emoflon.neo.emsl.eMSL.TargetNAC

class BWD implements Operation {

	/*
	 * --------------------------------
	 * GT rule generation methods
	 * --------------------------------
	 */

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
	
	override handleParameters(Map<Parameter, ParameterData> paramsToData, Map<String, Collection<Parameter>> paramGroups) {
		 for(group : paramGroups.values) {
		 	val representative = paramsToData.get(group.findFirst[param | paramsToData.get(param).domain.equals(ParameterDomain.TRG)])
		 	if(representative !== null) {
		 		for(param : group)
		 			paramsToData.get(param).map(representative.containingBlock, representative.containingPropertyName)
		 		representative.map(null, null)
		 	}
		 }
	}
	
	override getConditionOperator(ConditionOperator propOp, boolean isSrc) {
		if(!isSrc && propOp === ConditionOperator.ASSIGN)
			ConditionOperator.EQ.literal
		else
			propOp.literal
	}

	override preprocessNACs(Iterable<TripleRuleNAC> nacs) {
		return nacs.reject[it instanceof TargetNAC]
	}

	override requiresSrcModelRule() {
		true
	}
	
	override requiresTrgModelRule() {
		false
	}
	
	override requiresModelCreation() {
		true
	}
	
	override requiresCorrModelCreation() {
		true
	}

	/*
	 * --------------------------------
	 * app generation methods
	 * --------------------------------
	 */
	
	override additionalImports() {
		// TODO
		'''
		'''
	}
	
	override additionalFields(String tggName) {
		// TODO
		'''
		'''
	}
	
	override createGeneratorMethodBody(String tggName) {
		// TODO
		'''
		'''
	}
	
	override additionalMethods() {
		// TODO
		'''
		'''
	}
}
