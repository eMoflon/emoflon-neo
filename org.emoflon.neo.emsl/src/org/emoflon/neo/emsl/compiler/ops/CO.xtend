package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.ILPOperation
import org.emoflon.neo.emsl.eMSL.Action
import java.util.Collection
import org.emoflon.neo.emsl.eMSL.Parameter
import org.emoflon.neo.emsl.compiler.ParameterData
import java.util.Map

class CO extends ILPOperation {

	/*
	 * --------------------------------
	 * GT rule generation methods
	 * --------------------------------
	 */

	override String getNameExtension() {
		return "_CO"
	}
	
	override getAction(Action action, boolean isSrc) {
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
			import org.emoflon.neo.engine.modules.matchreprocessors.NoOpReprocessor;
			import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
			import org.emoflon.neo.engine.modules.ruleschedulers.AllRulesAllMatchesScheduler;
			import org.emoflon.neo.engine.modules.startup.NoOpStartup;
			import org.emoflon.neo.engine.modules.terminationcondition.OneShotTerminationCondition;
			import org.emoflon.neo.engine.modules.updatepolicies.CheckOnlyOperationalStrategy;
			import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
			import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;
		'''
	}
	
	override additionalFields(String tggName) {
		'''
			private static final SupportedILPSolver solver = SupportedILPSolver.Gurobi;
			private CheckOnlyOperationalStrategy checkOnly;
		'''
	}
	
	override createGeneratorMethodBody(String tggName, String packageName) {
		val fullOpName = '''«tggName»«nameExtension»'''
		'''
			var genAPI = new API_«tggName»_GEN(builder);
			var coAPI = new API_«fullOpName»(builder);
			checkOnly = new CheckOnlyOperationalStrategy(//
					solver, //
					genAPI.getAllRulesFor«tggName.toFirstUpper»_GEN(), //
					coAPI.getAllRulesFor«tggName.toFirstUpper»_CO(), //
					getNegativeConstraints(builder), //
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
					new NoOpReprocessor(), //
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
			
			protected Collection<IConstraint> getNegativeConstraints(NeoCoreBuilder builder) {
				return Collections.emptyList();
			}
		'''
	}
	
	override exportMetamodels() {
		false
	}
}
