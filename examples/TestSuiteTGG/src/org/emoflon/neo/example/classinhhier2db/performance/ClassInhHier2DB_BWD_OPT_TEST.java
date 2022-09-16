package org.emoflon.neo.example.classinhhier2db.performance;

import java.util.List;
import java.util.stream.Collectors;

import org.emoflon.neo.api.classinhhier2db.API_ClassInhHier2DB;
import org.emoflon.neo.api.classinhhier2db.run.ClassInhHier2DB_BWD_OPT_Run;
import org.emoflon.neo.api.classinhhier2db.tgg.API_ClassInhHier2DB_BWD_OPT;
import org.emoflon.neo.api.classinhhier2db.tgg.API_ClassInhHier2DB_GEN;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;
import org.emoflon.neo.engine.modules.matchreprocessors.BWD_OPTReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.BWD_OPTRuleScheduler;
import org.emoflon.neo.engine.modules.startup.NoOpStartup;
import org.emoflon.neo.engine.modules.terminationcondition.NoMoreMatchesTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.BackwardTransformationOperationalStrategy;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;

class ClassInhHier2DB_BWD_OPT_TEST extends ClassInhHier2DB_BWD_OPT_Run {
	
	public ClassInhHier2DB_BWD_OPT_TEST(String srcModelName, String trgModelName) {
		super(srcModelName, trgModelName);
	}

	@Override
	public NeoGenerator createGenerator(NeoCoreBuilder builder) {
		var api = new API_ClassInhHier2DB(builder);
		var genAPI = new API_ClassInhHier2DB_GEN(builder);
		var bwd_optAPI = new API_ClassInhHier2DB_BWD_OPT(builder);
		// remove ignore rules
		List<String> ignoreRules = List.of("SubClassToTableRule");
		var genRules = genAPI.getAllRulesForClassInhHier2DB_GEN().stream().filter(r -> !ignoreRules.contains(r.getName())).collect(Collectors.toList());
		var tripleRules = new API_ClassInhHier2DB(builder).getTripleRulesOfClassInhHier2DB().stream().filter(r -> !ignoreRules.contains(r.getName())).collect(Collectors.toList());
		var analyser = new TripleRuleAnalyser(tripleRules.stream().filter(r -> !ignoreRules.contains(r.getName())).collect(Collectors.toList()));
		var bwd_optRules = bwd_optAPI.getAllRulesForClassInhHier2DB_BWD_OPT().stream().filter(r -> !ignoreRules.contains(r.getName())).collect(Collectors.toList());
		
		backwardTransformation = new BackwardTransformationOperationalStrategy(//
				solver, //
				builder, //
				genRules, //
				bwd_optRules, //
				api.getConstraintsOfClassInhHier2DB(), //
				srcModelName, //
				trgModelName//
		);
		
		return new NeoGenerator(//
				bwd_optRules, //
				new NoOpStartup(), //
				new NoMoreMatchesTerminationCondition(), //
				new BWD_OPTRuleScheduler(analyser), //
				backwardTransformation, //
				new BWD_OPTReprocessor(analyser), //
				backwardTransformation, //
				new HeartBeatAndReportMonitor(), //
				new ModelNameValueGenerator(srcModelName, trgModelName), //
				List.of(new LoremIpsumStringValueGenerator()));
	}

}