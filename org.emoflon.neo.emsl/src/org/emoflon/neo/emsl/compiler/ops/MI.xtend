package org.emoflon.neo.emsl.compiler.ops

import java.util.ArrayList
import java.util.Collection
import java.util.Map
import org.emoflon.neo.emsl.compiler.ILPOperation
import org.emoflon.neo.emsl.compiler.ParameterData
import org.emoflon.neo.emsl.eMSL.Action
import org.emoflon.neo.emsl.eMSL.ActionOperator
import org.emoflon.neo.emsl.eMSL.Parameter
import org.emoflon.neo.emsl.compiler.TGGCompilerValidations
import org.emoflon.neo.emsl.eMSL.ConditionOperator

class MI extends ILPOperation {

	/*
	 * --------------------------------
	 * GT rule generation methods
	 * --------------------------------
	 */
	override String getNameExtension() {
		return "_MI"
	}

	override getAction(Action action, Domain domain) {
		if(action === null || !ActionOperator::CREATE.equals(action.getOp())) return "" else return "++"
	}

	override getConditionOperator(ConditionOperator propOp, Domain domain) {
		propOp.literal
	}

	override requiresCorrModelCreation() {
		true
	}

	override handleParameters(Map<Parameter, ParameterData> paramsToData,
		Map<String, Collection<Parameter>> paramGroups, int ruleID, ArrayList<String> greenElements) {

		for (group : paramGroups.values) {
			val representative = paramsToData.get(selectParamGroupRepresentative(group, paramsToData, ruleID, greenElements))
			if (representative !== null) {
				for (param : group)
					paramsToData.get(param).map(representative.containingBlock, representative.containingPropertyName)
				representative.unmap()
			}
		}
	}

	override selectParamGroupRepresentative(Collection<Parameter> paramGroup,
		Map<Parameter, ParameterData> paramsToData, int ruleID, ArrayList<String> greenElements) {
			if (greenElements === null)
				return paramGroup.head;
			for (Parameter p : paramGroup) {
				if (TGGCompilerValidations.binaryAND(ruleID, Math.pow(2, greenElements.indexOf(paramsToData.get(p).containingBlock.name)).intValue) == 0)
					return p;
			}
		return null;
	}

	/*
	 * --------------------------------
	 * app generation methods
	 * --------------------------------
	 */
	override additionalImports(String tggName, String packagePath) {
		'''
			import static «packagePath».run.«tggName»_GEN_Run.SRC_MODEL_NAME;
			import static «packagePath».run.«tggName»_GEN_Run.TRG_MODEL_NAME;
					
			import java.util.Collection;
			import java.util.Collections;
			import java.util.List;
			import org.emoflon.neo.engine.api.constraints.IConstraint;
			import org.emoflon.neo.api.«packagePath».API_«tggName»_GEN;
			import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
			import org.emoflon.neo.engine.modules.matchreprocessors.MIReprocessor;
			import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
			import org.emoflon.neo.engine.modules.ruleschedulers.MIRuleScheduler;
			import org.emoflon.neo.engine.modules.startup.PrepareContextDeltaAttributes; 
			import org.emoflon.neo.engine.modules.terminationcondition.NoMoreMatchesTerminationCondition;
			import org.emoflon.neo.engine.modules.updatepolicies.ModelIntegrationOperationalStrategy;
			import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
			import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;
			import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;
		'''
	}

	override additionalFields(String tggName) {
		'''
			private static final SupportedILPSolver solver = SupportedILPSolver.Gurobi;
			private ModelIntegrationOperationalStrategy modelIntegration;
		'''
	}

	override createGeneratorMethodBody(String tggName, String packageName) {
		val fullOpName = '''«tggName»«nameExtension»'''
		'''
			var genAPI = new API_«tggName»_GEN(builder);
			var miAPI = new API_«fullOpName»(builder);
			var genRules = genAPI.getAllRulesFor«tggName.toFirstUpper»_GEN();
			var analyser = new TripleRuleAnalyser(new API_«packageName»(builder).getTripleRulesOf«tggName.toFirstUpper»());
			modelIntegration = new ModelIntegrationOperationalStrategy(//
					solver, //
					builder, //
					genRules, //
					miAPI.getAllRulesFor«fullOpName.toFirstUpper»(), //
					getNegativeConstraints(builder), //
					srcModelName, //
					trgModelName//
			);
			
			return new NeoGenerator(//
					miAPI.getAllRulesFor«fullOpName.toFirstUpper»(), //
					new PrepareContextDeltaAttributes(builder, srcModelName, trgModelName), //
					new NoMoreMatchesTerminationCondition(), //
					new MIRuleScheduler(analyser), //
					modelIntegration, //
					new MIReprocessor(analyser), //
					modelIntegration, //
					new HeartBeatAndReportMonitor(), //
					new ModelNameValueGenerator(srcModelName, trgModelName), //
					List.of(new LoremIpsumStringValueGenerator()));
		'''
	}

	override additionalMethods() {
		'''
			
			public ModelIntegrationOperationalStrategy runModelIntegration() throws Exception {
				run();
				return modelIntegration;
			}
			
			protected Collection<IConstraint> getNegativeConstraints(NeoCoreBuilder builder) {
				return Collections.emptyList();
			}
		'''
	}

	override exportMetamodels() {
		false
	}

	override boolean isMulti() {
		true
	}

//	override isComposed() {
//		true
//	}
//	
//	override getSubOps() {
//		return (#[new CO(), new CC(), new BWD_OPT(), new FWD_OPT()])
//	}
//	
	override getDeltaCondition(Action action) {
		if (action === null || !ActionOperator::CREATE.equals(action.getOp()))
			return "~_ex_ : true"
		else
			return ""
	}

	override String getAction(Action action, int ruleID, ArrayList<String> greenElements, String element) {
		if (greenElements === null)
			return ""
		if (TGGCompilerValidations.binaryAND(ruleID, Math.pow(2, greenElements.indexOf(element)).intValue) > 0)
			return getAction(action, null)
		else
			return ""

	}

	override String getConditionOperator(ConditionOperator propOp, int ruleID, ArrayList<String> greenElements,
		String element) {
		if (greenElements === null)
			return super.getConditionOperator(propOp, null)
		if (TGGCompilerValidations.binaryAND(ruleID, Math.pow(2, greenElements.indexOf(element)).intValue) > 0)
			return getConditionOperator(propOp, null)
		else
			return super.getConditionOperator(propOp, null)
	}
	
	override selectParamGroupRepresentative(Collection<Parameter> paramGroup, Map<Parameter, ParameterData> paramsToData) {
		paramGroup.head
	}
	
}
