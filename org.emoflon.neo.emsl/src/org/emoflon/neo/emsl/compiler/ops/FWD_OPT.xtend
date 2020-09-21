package org.emoflon.neo.emsl.compiler.ops

import java.util.Collection
import java.util.Map
import org.emoflon.neo.emsl.compiler.ILPOperation
import org.emoflon.neo.emsl.compiler.ParameterData
import org.emoflon.neo.emsl.compiler.TGGCompilerUtils.ParameterDomain
import org.emoflon.neo.emsl.eMSL.Action
import org.emoflon.neo.emsl.eMSL.ActionOperator
import org.emoflon.neo.emsl.eMSL.ConditionOperator
import org.emoflon.neo.emsl.eMSL.Parameter

class FWD_OPT extends ILPOperation {
	override String getNameExtension() {
		return "_FWD_OPT"
	}
	
	override String getAction(Action action, Domain domain) {
		if(domain.equals(Domain.SRC) || action === null || !ActionOperator::CREATE.equals(action.getOp())) return "" else return "++"
	}
	
	override getConditionOperator(ConditionOperator propOp, Domain domain) {
		if(domain.equals(Domain.SRC))
			super.getConditionOperator(propOp, domain)
		else
			propOp.literal
	}
	
	override requiresCorrModelCreation() {
		true
	}
	
	override selectParamGroupRepresentative(Collection<Parameter> paramGroup, Map<Parameter, ParameterData> paramsToData) {
		paramGroup.findFirst[param | paramsToData.get(param).domain.equals(ParameterDomain.SRC)]
	}
	
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
			import org.emoflon.neo.engine.modules.matchreprocessors.FWD_OPTReprocessor;
			import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
			import org.emoflon.neo.engine.modules.ruleschedulers.FWD_OPTRuleScheduler;
			import org.emoflon.neo.engine.modules.startup.NoOpStartup;
			import org.emoflon.neo.engine.modules.terminationcondition.NoMoreMatchesTerminationCondition;
			import org.emoflon.neo.engine.modules.updatepolicies.ForwardTransformationOperationalStrategy;
			import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
			import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;
			import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;
		'''
	}
	
	override additionalFields(String tggName, String solver) {
		'''
			private static final SupportedILPSolver solver = SupportedILPSolver.«solver»;
			private ForwardTransformationOperationalStrategy forwardTransformation;
		'''
	}
	
	override createGeneratorMethodBody(String tggName, String packageName) {
		val fullOpName = '''«tggName»«nameExtension»'''
		'''
			var genAPI = new API_«tggName»_GEN(builder);
			var fwd_optAPI = new API_«fullOpName»(builder);
			var genRules = genAPI.getAllRulesFor«tggName.toFirstUpper»_GEN();
			var analyser = new TripleRuleAnalyser(new API_«packageName»(builder).getTripleRulesOf«tggName.toFirstUpper»());
			forwardTransformation = new ForwardTransformationOperationalStrategy(//
					solver, //
					builder, //
					genRules, //
					fwd_optAPI.getAllRulesFor«fullOpName.toFirstUpper»(), //
					getNegativeConstraints(builder), //
					srcModelName, //
					trgModelName//
			);

			return new NeoGenerator(//
					fwd_optAPI.getAllRulesFor«fullOpName.toFirstUpper»(), //
					new NoOpStartup(), //
					new NoMoreMatchesTerminationCondition(), //
					new FWD_OPTRuleScheduler(analyser), //
					forwardTransformation, //
					new FWD_OPTReprocessor(analyser), //
					forwardTransformation, //
					new HeartBeatAndReportMonitor(), //
					new ModelNameValueGenerator(srcModelName, trgModelName), //
					List.of(new LoremIpsumStringValueGenerator()));
		'''
	}
	
	override additionalMethods() {
		'''

			public ForwardTransformationOperationalStrategy runForwardTransformation() throws Exception {
				run();
				return forwardTransformation;
			}

			protected Collection<IConstraint> getNegativeConstraints(NeoCoreBuilder builder) {
				return Collections.emptyList();
			}
		'''
	}
	
	override exportMetamodels() {
		false
	}
}