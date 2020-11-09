package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.ILPOperation
import org.emoflon.neo.emsl.eMSL.Action
import java.util.Collection
import org.emoflon.neo.emsl.eMSL.Parameter
import org.emoflon.neo.emsl.compiler.ParameterData
import java.util.Map

class CC extends ILPOperation {

	/*
	 * --------------------------------
	 * GT rule generation methods
	 * --------------------------------
	 */

	override String getNameExtension() {
		return "_CC"
	}
	
	override getAction(Action action, boolean isSrc) {
		return ""
	}

	override requiresCorrModelCreation() {
		true
	}
	
	override selectParamGroupRepresentative(Collection<Parameter> paramGroup, Map<Parameter, ParameterData> paramsToData) {
		paramGroup.head
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
			import org.emoflon.neo.engine.modules.matchreprocessors.CCReprocessor;
			import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
			import org.emoflon.neo.engine.modules.ruleschedulers.CCRuleScheduler;
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
			private CorrCreationOperationalStrategy corrCreation;
		'''
	}
	
	override createGeneratorMethodBody(String tggName, String packageName) {
		val fullOpName = '''«tggName»«nameExtension»'''
		'''
			var api = new API_«packageName»(builder);
			var genAPI = new API_«tggName»_GEN(builder);
			var ccAPI = new API_«fullOpName»(builder);
			var genRules = genAPI.getAllRulesFor«tggName.toFirstUpper»_GEN();
			var analyser = new TripleRuleAnalyser(new API_«packageName»(builder).getTripleRulesOf«tggName.toFirstUpper»());
			corrCreation = new CorrCreationOperationalStrategy(//
					solver, //
					builder, //
					genRules, //
					ccAPI.getAllRulesFor«fullOpName.toFirstUpper»(), //
					api.getConstraintsOf«tggName.toFirstUpper»(), //
					srcModelName, //
					trgModelName//
			);

			return new NeoGenerator(//
					ccAPI.getAllRulesFor«fullOpName.toFirstUpper»(), //
					new NoOpStartup(), //
					new NoMoreMatchesTerminationCondition(), //
					new CCRuleScheduler(analyser), //
					corrCreation, //
					new CCReprocessor(analyser), //
					corrCreation, //
					new HeartBeatAndReportMonitor(), //
					new ModelNameValueGenerator(srcModelName, trgModelName), //
					List.of(new LoremIpsumStringValueGenerator()));
		'''
	}
	
	override additionalMethods() {
		'''
		
			public CorrCreationOperationalStrategy runCorrCreation() throws Exception {
				run();
				return corrCreation;
			}
		'''
	}
	
	override exportMetamodels() {
		false
	}
}
