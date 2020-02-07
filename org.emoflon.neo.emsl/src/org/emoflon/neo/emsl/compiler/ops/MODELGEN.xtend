package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.Operation
import org.emoflon.neo.emsl.eMSL.Action
import org.emoflon.neo.emsl.eMSL.ActionOperator
import java.util.Map
import org.emoflon.neo.emsl.eMSL.Parameter
import java.util.Collection
import org.emoflon.neo.emsl.eMSL.ConditionOperator
import org.emoflon.neo.emsl.compiler.ParameterData
import org.emoflon.neo.emsl.eMSL.TripleRuleNAC

class MODELGEN implements Operation {

	/*
	 * --------------------------------
	 * GT rule generation methods
	 * --------------------------------
	 */

	override String getNameExtension() {
		return "_GEN"
	}

	override String getAction(Action action, boolean isSrc) {
		if(action === null || !ActionOperator::CREATE.equals(action.getOp())) return "" else return "++"
	}

	override String getTranslation(Action action, boolean isSrc) {
		return ""
	}
	
	override handleParameters(Map<Parameter, ParameterData> paramsToData, Map<String, Collection<Parameter>> paramGroups) {
		// Nothing to do here, all parameters are already mapped to their correct String representation
	}
	
	override getConditionOperator(ConditionOperator propOp, boolean isSrc) {
		propOp.literal
	}

	override Iterable<TripleRuleNAC> preprocessNACs(Iterable<TripleRuleNAC> nacs) {
		return nacs
	}

	override requiresCorrModelCreation() {
		true
	}

	/*
	 * --------------------------------
	 * app generation methods
	 * --------------------------------
	 */
	
	override additionalImports(String tggName, String packagePath) {
		'''
			import org.emoflon.neo.engine.generator.INodeSampler;
			import org.emoflon.neo.engine.modules.cleanup.NoOpCleanup;
			import org.emoflon.neo.engine.modules.matchreprocessors.ParanoidNeoReprocessor;
			import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
			import org.emoflon.neo.engine.modules.ruleschedulers.TwoPhaseRuleSchedulerForGEN;
			import org.emoflon.neo.engine.modules.startup.NoOpStartup;
			import org.emoflon.neo.engine.modules.terminationcondition.CompositeTerminationConditionForGEN;
			import org.emoflon.neo.engine.modules.terminationcondition.MaximalRuleApplicationsTerminationCondition;
			import org.emoflon.neo.engine.modules.updatepolicies.TwoPhaseUpdatePolicyForGEN;
			import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
			import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;
			import java.util.List;
			import java.util.concurrent.TimeUnit;
		'''
	}
	
	override additionalFields(String tggName) {
		'''
			public static final String SRC_MODEL_NAME = "«tggName»_Source";
			public static final String TRG_MODEL_NAME = "«tggName»_Target";
		'''
	}
	
	override createGeneratorMethodBody(String tggName, String packageName) {
		val fullOpName = '''«tggName»«nameExtension»'''
		'''
			var allRules = new API_«fullOpName»(builder).getAllRulesFor«fullOpName.toFirstUpper»();
			var maxRuleApps = new MaximalRuleApplicationsTerminationCondition(allRules, -1);
			
			INodeSampler sampler = (String type, String ruleName, String nodeName) -> {
				return INodeSampler.EMPTY;
			};
			
			return new NeoGenerator(//
					allRules, //
					new NoOpStartup(), //
					new CompositeTerminationConditionForGEN(2, TimeUnit.MINUTES, maxRuleApps), //
					new TwoPhaseRuleSchedulerForGEN(sampler), //
					new TwoPhaseUpdatePolicyForGEN(maxRuleApps), //
					new ParanoidNeoReprocessor(), //
					new NoOpCleanup(), //
					new HeartBeatAndReportMonitor(), //
					new ModelNameValueGenerator(srcModelName, trgModelName), //
					List.of(new LoremIpsumStringValueGenerator()));
		'''
	}
	
	override additionalMethods() {
		'''
		'''
	}
	
	override exportMetamodels() {
		true
	}
}
