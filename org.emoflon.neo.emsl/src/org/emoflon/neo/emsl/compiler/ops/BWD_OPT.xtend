package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.ILPOperation
import org.emoflon.neo.emsl.eMSL.Action
import org.emoflon.neo.emsl.eMSL.ActionOperator
import org.emoflon.neo.emsl.eMSL.ConditionOperator
import java.util.Collection
import org.emoflon.neo.emsl.eMSL.Parameter
import org.emoflon.neo.emsl.compiler.ParameterData
import java.util.Map
import org.emoflon.neo.emsl.compiler.TGGCompilerUtils.ParameterDomain

class BWD_OPT extends ILPOperation {
	override String getNameExtension() {
		return "_BWD_OPT"
	}
	
	override String getAction(Action action, boolean isSrc) {
		if(!isSrc || action === null || !ActionOperator::CREATE.equals(action.getOp())) return "" else return "++"
	}
	
	override getConditionOperator(ConditionOperator propOp, boolean isSrc) {
		if(!isSrc)
			super.getConditionOperator(propOp, isSrc)
		else
			propOp.literal
	}
	
	override requiresCorrModelCreation() {
		true
	}
	
	override selectParamGroupRepresentative(Collection<Parameter> paramGroup, Map<Parameter, ParameterData> paramsToData) {
		paramGroup.findFirst[param | paramsToData.get(param).domain.equals(ParameterDomain.TRG)]
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
			import org.emoflon.neo.engine.modules.matchreprocessors.BWD_OPTReprocessor;
			import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
			import org.emoflon.neo.engine.modules.ruleschedulers.BWD_OPTRuleScheduler;
			import org.emoflon.neo.engine.modules.startup.NoOpStartup;
			import org.emoflon.neo.engine.modules.terminationcondition.NoMoreMatchesTerminationCondition;
			import org.emoflon.neo.engine.modules.updatepolicies.CorrCreationOperationalStrategy;
			import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
			import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;
			import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;
		'''
	}
	
	override additionalFields(String tggName) {
		'''
			private static final SupportedILPSolver solver = SupportedILPSolver.Sat4J;
			private CorrCreationOperationalStrategy backwardTransformation;
		'''
	}
	
	override createGeneratorMethodBody(String tggName, String packageName) {
		val fullOpName = '''«tggName»«nameExtension»'''
		'''
			var api = new API_«packageName»(builder);
			var genAPI = new API_«tggName»_GEN(builder);
			var bwd_optAPI = new API_«fullOpName»(builder);
			var genRules = genAPI.getAllRulesFor«tggName.toFirstUpper»_GEN();
			var analyser = new TripleRuleAnalyser(new API_«packageName»(builder).getTripleRulesOf«tggName.toFirstUpper»());
			backwardTransformation = new CorrCreationOperationalStrategy(//
					solver, //
					builder, //
					genRules, //
					bwd_optAPI.getAllRulesFor«fullOpName.toFirstUpper»(), //
					api.getConstraintsOf«tggName.toFirstUpper»(), //
					srcModelName, //
					trgModelName//
			);

			return new NeoGenerator(//
					bwd_optAPI.getAllRulesFor«fullOpName.toFirstUpper»(), //
					new NoOpStartup(), //
					new NoMoreMatchesTerminationCondition(), //
					new BWD_OPTRuleScheduler(analyser), //
					backwardTransformation, //
					new BWD_OPTReprocessor(analyser), //
					backwardTransformation, //
					new HeartBeatAndReportMonitor(), //
					new ModelNameValueGenerator(srcModelName, trgModelName), //
					List.of(new LoremIpsumStringValueGenerator()));
		'''
	}
	
	override additionalMethods() {
		'''

			public CorrCreationOperationalStrategy runBackwardTransformation() throws Exception {
				run();
				return backwardTransformation;
			}
		'''
	}
	
	override exportMetamodels() {
		false
	}
}