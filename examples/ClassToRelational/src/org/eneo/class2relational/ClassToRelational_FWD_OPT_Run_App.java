// All boiler plate, setup

package org.eneo.class2relational;

import java.util.List;

import org.emoflon.neo.api.classtorelational.API_ClassToRelational;
import org.emoflon.neo.api.classtorelational.run.ClassToRelational_FWD_OPT_Run;
import org.emoflon.neo.api.classtorelational.tgg.API_ClassToRelational_FWD_OPT;
import org.emoflon.neo.api.classtorelational.tgg.API_ClassToRelational_GEN;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
import org.emoflon.neo.engine.modules.matchreprocessors.FWD_OPTReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.FWD_OPTRuleScheduler;
import org.emoflon.neo.engine.modules.startup.NoOpStartup;
import org.emoflon.neo.engine.modules.terminationcondition.NoMoreMatchesTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.ForwardTransformationOperationalStrategy;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;

public class ClassToRelational_FWD_OPT_Run_App extends ClassToRelational_FWD_OPT_Run {
	public ClassToRelational_FWD_OPT_Run_App(String srcModelName, String trgModelName) {
		super(srcModelName, trgModelName, SupportedILPSolver.Sat4J);
	}

	public NeoGenerator createGenerator(NeoCoreBuilder builder) {
		var api = new API_ClassToRelational(builder);
		var genAPI = new API_ClassToRelational_GEN(builder);
		var fwd_optAPI = new API_ClassToRelational_FWD_OPT(builder);
		var genRules = genAPI.getAllRulesForClassToRelational_GEN();
		var analyser = new TripleRuleAnalyser(new API_ClassToRelational(builder).getTripleRulesOfClassToRelational());

		var choiceOfFwdRules = fwd_optAPI.getAllRulesForClassToRelational_FWD_OPT();

		forwardTransformation = new ForwardTransformationOperationalStrategy(//
				solver, //
				builder, //
				genRules, //
				choiceOfFwdRules, //
				api.getConstraintsOfClassToRelational(), //
				srcModelName, //
				trgModelName//
		);

		var gen = new NeoGenerator(//
				choiceOfFwdRules, //
				new NoOpStartup(), //
				new NoMoreMatchesTerminationCondition(), //
				new FWD_OPTRuleScheduler(analyser), //
				forwardTransformation, //
				new FWD_OPTReprocessor(analyser), //
				forwardTransformation, //
				new HeartBeatAndReportMonitor(), //
				new ModelNameValueGenerator(srcModelName, trgModelName), //
				List.of(new LoremIpsumStringValueGenerator()));
		
		gen.getAttrConstrContainer().addCreator("firstToLowerCase", () -> new FirstToLowerCase());

		return gen;
	}
}
