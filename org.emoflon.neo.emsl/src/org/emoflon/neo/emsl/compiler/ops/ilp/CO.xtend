package org.emoflon.neo.emsl.compiler.ops.ilp

import java.util.Collection
import java.util.Map
import org.emoflon.neo.emsl.compiler.ParameterData
import org.emoflon.neo.emsl.eMSL.Action
import org.emoflon.neo.emsl.eMSL.Parameter

class CO extends ILPOperation {

	/*
	 * --------------------------------
	 * GT rule generation methods
	 * --------------------------------
	 */

	override String getNameExtension() {
		return "_CO"
	}
	
	override getAction(Action action, Domain domain) {
		return ""
	}

	override requiresCorrModelCreation() {
		false
	}
	
	override selectParamGroupRepresentative(Collection<Parameter> paramGroup, Map<Parameter, ParameterData> paramsToData) {
		paramGroup.head
	}

	/*
	 * --------------------------------
	 * app generation methods
	 * --------------------------------
	 */
	
	override additionalFields(String tggName, String solver) {
		'''
			protected static SupportedILPSolver solver = SupportedILPSolver.«solver»;
			protected CheckOnlyOperationalStrategy checkOnly;
		'''
	}
	
	override createGeneratorMethodBody(String tggName, String packageName) {
		val fullOpName = '''«tggName»«nameExtension»'''
		'''
			var api = new API_«packageName»(builder);
			var genAPI = new API_«tggName»_GEN(builder);
			var coAPI = new API_«fullOpName»(builder);
			var analyser = new TripleRuleAnalyser(new API_«packageName»(builder).getTripleRulesOf«tggName.toFirstUpper»());
			checkOnly = new CheckOnlyOperationalStrategy(//
					solver, //
					genAPI.getAllRulesFor«tggName.toFirstUpper»_GEN(), //
					coAPI.getAllRulesFor«fullOpName.toFirstUpper»(), //
					api.getConstraintsOf«tggName.toFirstUpper»(), //
					builder, //
					srcModelName, //
					trgModelName//
			);

			return new NeoGenerator(//
					coAPI.getAllRulesFor«fullOpName.toFirstUpper»(), //
					new NoOpStartup(), //
					new OneShotTerminationCondition(), //
					new AllRulesAllMatchesScheduler(), //
					checkOnly, //
					new COReprocessor(analyser), //
					checkOnly, //
					new HeartBeatAndReportMonitor(), //
					new ModelNameValueGenerator(srcModelName, trgModelName), //
					List.of(new LoremIpsumStringValueGenerator()));
		'''
	}
	
	override additionalMethods() {
		'''
			
			public CheckOnlyOperationalStrategy runCheckOnly() throws Exception {
				run();
				return checkOnly;
			}
		'''
	}
	
	override exportMetamodels() {
		false
	}
	
	override additionalImports(String tggName, String packagePath) {
		'''
			«super.additionalImports(tggName, packagePath)»
			import org.emoflon.neo.engine.modules.updatepolicies.CheckOnlyOperationalStrategy;
			import org.emoflon.neo.engine.modules.terminationcondition.OneShotTerminationCondition;
			import org.emoflon.neo.engine.modules.ruleschedulers.AllRulesAllMatchesScheduler;
			import org.emoflon.neo.engine.modules.matchreprocessors.COReprocessor;
		'''
	}
}
