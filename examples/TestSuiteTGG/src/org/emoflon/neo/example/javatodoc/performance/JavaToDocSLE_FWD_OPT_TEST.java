package org.emoflon.neo.example.javatodoc.performance;

import java.util.List;
import java.util.stream.Collectors;

import org.emoflon.neo.api.javatodocsle.API_JavaToDocSLE;
import org.emoflon.neo.api.javatodocsle.run.JavaToDocSLE_FWD_OPT_Run;
import org.emoflon.neo.api.javatodocsle.tgg.API_JavaToDocSLE_FWD_OPT;
import org.emoflon.neo.api.javatodocsle.tgg.API_JavaToDocSLE_GEN;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;
import org.emoflon.neo.engine.modules.matchreprocessors.FWD_OPTReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.FWD_OPTRuleScheduler;
import org.emoflon.neo.engine.modules.startup.NoOpStartup;
import org.emoflon.neo.engine.modules.terminationcondition.NoMoreMatchesTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.ForwardTransformationOperationalStrategy;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;

class JavaToDocSLE_FWD_OPT_TEST extends JavaToDocSLE_FWD_OPT_Run {
	
	public JavaToDocSLE_FWD_OPT_TEST(String srcModelName, String trgModelName) {
		super(srcModelName, trgModelName);
	}

	@Override
	public NeoGenerator createGenerator(NeoCoreBuilder builder) {
		var api = new API_JavaToDocSLE(builder);
		var genAPI = new API_JavaToDocSLE_GEN(builder);
		var fwd_optAPI = new API_JavaToDocSLE_FWD_OPT(builder);
		
		// remove ignore rules
		List<String> ignoreRules = List.of("AddGlossaryRule", "AddGlossaryEntryRule", "LinkGlossaryEntryRule");
		var tripleRules = new API_JavaToDocSLE(builder).getTripleRulesOfJavaToDocSLE();
		var genRules = genAPI.getAllRulesForJavaToDocSLE_GEN().stream().filter(r -> !ignoreRules.contains(r.getName())).collect(Collectors.toList());
		var fwd_optRules = fwd_optAPI.getAllRulesForJavaToDocSLE_FWD_OPT().stream().filter(r -> !ignoreRules.contains(r.getName())).collect(Collectors.toList());
		var analyser = new TripleRuleAnalyser(tripleRules.stream().filter(r -> !ignoreRules.contains(r.getName())).collect(Collectors.toList()));
		
		forwardTransformation = new ForwardTransformationOperationalStrategy(//
				solver, //
				builder, //
				genRules, //
				fwd_optRules, //
				api.getConstraintsOfJavaToDocSLE(), //
				srcModelName, //
				trgModelName//
		);
		
		return new NeoGenerator(//
				fwd_optRules, //
				new NoOpStartup(), //
				new NoMoreMatchesTerminationCondition(), //
				new FWD_OPTRuleScheduler(analyser), //
				forwardTransformation, //
				new FWD_OPTReprocessor(analyser), //
				forwardTransformation, //
				new HeartBeatAndReportMonitor(), //
				new ModelNameValueGenerator(srcModelName, trgModelName), //
				List.of(new LoremIpsumStringValueGenerator()));
	}

}