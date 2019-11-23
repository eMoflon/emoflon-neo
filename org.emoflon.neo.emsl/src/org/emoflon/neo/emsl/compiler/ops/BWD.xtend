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
	
	override additionalImports(String tggName) {
		'''
			import static run.«tggName»_GEN_Run.SRC_MODEL_NAME;
			import static run.«tggName»_GEN_Run.TRG_MODEL_NAME;
			
			import org.emoflon.neo.engine.modules.cleanup.RemoveTranslateAttributes;
			import org.emoflon.neo.engine.modules.matchreprocessors.ParanoidNeoReprocessor;
			import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
			import org.emoflon.neo.engine.modules.ruleschedulers.AllRulesAllMatchesScheduler;
			import org.emoflon.neo.engine.modules.startup.PrepareTranslateAttributes;
			import org.emoflon.neo.engine.modules.terminationcondition.NoMoreMatchesTerminationCondition;
			import org.emoflon.neo.engine.modules.updatepolicies.AnySingleMatchUpdatePolicy;
			import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
			import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;
			import java.util.List;
		'''
	}
	
	override additionalFields(String tggName) {
		'''
		'''
	}
	
	override createGeneratorMethodBody(String tggName) {
		// TODO replace modules with better ones
		
		val fullOpName = '''«tggName»«nameExtension»'''
		'''
			return new NeoGenerator(//
					new API_«fullOpName»(builder).getAllRulesFor«fullOpName»(), //
					new PrepareTranslateAttributes(builder, TRG_MODEL_NAME), //
					new NoMoreMatchesTerminationCondition(), //
					new AllRulesAllMatchesScheduler(), //
					new AnySingleMatchUpdatePolicy(), //
					new ParanoidNeoReprocessor(), //
					new RemoveTranslateAttributes(builder, TRG_MODEL_NAME), //
					new HeartBeatAndReportMonitor(), //
					new ModelNameValueGenerator(SRC_MODEL_NAME, TRG_MODEL_NAME), //
					List.of(new LoremIpsumStringValueGenerator()));
		'''
	}
	
	override additionalMethods() {
		'''
		'''
	}
}
