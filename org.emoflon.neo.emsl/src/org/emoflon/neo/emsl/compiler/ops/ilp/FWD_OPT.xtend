package org.emoflon.neo.emsl.compiler.ops.ilp

import java.util.Collection
import java.util.Map
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
	
	override additionalFields(String tggName, String solver) {
		'''
			protected static SupportedILPSolver solver = SupportedILPSolver.«solver»;
			protected ForwardTransformationOperationalStrategy forwardTransformation;
		'''
	}
	
	override createGeneratorMethodBody(String tggName, String packageName) {
		val fullOpName = '''«tggName»«nameExtension»'''
		'''
			var api = new API_«packageName»(builder);
			var genAPI = new API_«tggName»_GEN(builder);
			var fwd_optAPI = new API_«fullOpName»(builder);
			var genRules = genAPI.getAllRulesFor«tggName.toFirstUpper»_GEN();
			var analyser = new TripleRuleAnalyser(new API_«packageName»(builder).getTripleRulesOf«tggName.toFirstUpper»());
			forwardTransformation = new ForwardTransformationOperationalStrategy(//
					solver, //
					builder, //
					genRules, //
					fwd_optAPI.getAllRulesFor«fullOpName.toFirstUpper»(), //
					api.getConstraintsOf«tggName.toFirstUpper»(), //
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
		'''
	}
	
	override exportMetamodels() {
		false
	}
	
	override additionalImports(String tggName, String packagePath) {
		'''
			«super.additionalImports(tggName, packagePath)»
			import org.emoflon.neo.engine.modules.updatepolicies.ForwardTransformationOperationalStrategy;
			import org.emoflon.neo.engine.modules.matchreprocessors.FWD_OPTReprocessor;
			import org.emoflon.neo.engine.modules.ruleschedulers.FWD_OPTRuleScheduler;
		'''
	}
}