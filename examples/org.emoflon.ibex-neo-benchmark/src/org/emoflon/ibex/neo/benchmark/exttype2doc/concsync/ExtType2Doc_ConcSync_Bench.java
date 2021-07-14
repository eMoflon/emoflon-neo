package org.emoflon.ibex.neo.benchmark.exttype2doc.concsync;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.emoflon.ibex.neo.benchmark.IntegrationBench;
import org.emoflon.neo.api.exttype2doc_concsync.API_ExtType2Doc_ConcSync;
import org.emoflon.neo.api.exttype2doc_concsync.tgg.API_ExtType2Doc_ConcSync_GEN;
import org.emoflon.neo.api.exttype2doc_concsync.tgg.API_ExtType2Doc_ConcSync_MI;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
import org.emoflon.neo.engine.modules.matchreprocessors.MIReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.MIRuleScheduler;
import org.emoflon.neo.engine.modules.startup.PrepareContextDeltaAttributes;
import org.emoflon.neo.engine.modules.terminationcondition.NoMoreMatchesTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.ModelIntegrationOperationalStrategy;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;

public class ExtType2Doc_ConcSync_Bench extends IntegrationBench<ExtType2Doc_ConcSync_Params> {
	
	protected int conflict_counter = 0;
	protected int conflict_solved_attr_counter = 0;
	protected int conflict_solved_delPres_counter = 0;
	protected int conflict_solved_move_counter = 0;

	public ExtType2Doc_ConcSync_Bench(String projectName, String projectPath) {
		super(projectName, projectPath);
	}

	@Override
	protected void applyDelta(ExtType2Doc_ConcSync_Params parameters) {
		
		for (int i=0; i < parameters.num_of_conflicts; i *=3 /* as there are three conflicts per iteration*/) {
			api.getRule_CreateDeleteConflict().rule().apply();
			api.getRule_MoveMoveConflict().rule().apply();
			api.getRule_MoveDeleteConflict().rule().apply();
		}
		
	}

	@Override
	public ModelIntegrationOperationalStrategy initOpStrat(NeoCoreBuilder builder, SupportedILPSolver solver) {
		var miAPI = new API_ExtType2Doc_ConcSync_MI(builder);
		var genAPI = new API_ExtType2Doc_ConcSync_GEN(builder);
		var genRules = genAPI.getAllRulesForExtType2Doc_ConcSync_GEN();
		
		return new ModelIntegrationOperationalStrategy(//
				solver, //
				builder, //
				genRules, //
				miAPI.getAllRulesForExtType2Doc_ConcSync_MI(), //
				getNegativeConstraints(builder), //
				filename_src, //
				filename_trg//
		);
	}

	@Override
	public NeoGenerator createGenerator(NeoCoreBuilder builder, SupportedILPSolver solver) {
		var miAPI = new API_ExtType2Doc_ConcSync_MI(builder);
		var tripleRules = new API_ExtType2Doc_ConcSync(builder).getTripleRulesOfExtType2Doc_ConcSync();
		var analyser = new TripleRuleAnalyser(tripleRules);
		var modelIntegration = initOpStrat(builder, solver);
		
		return new NeoGenerator(//
				miAPI.getAllRulesForExtType2Doc_ConcSync_MI(), //
				new PrepareContextDeltaAttributes(builder, filename_src, filename_trg), //
				new NoMoreMatchesTerminationCondition(), //
				new MIRuleScheduler(analyser), //
				modelIntegration, //
				new MIReprocessor(analyser), //
				modelIntegration, //
				new HeartBeatAndReportMonitor(), //
				new ModelNameValueGenerator(filename_src, filename_trg), //
				List.of(new LoremIpsumStringValueGenerator()));
	}
	
	protected Collection<IConstraint> getNegativeConstraints(NeoCoreBuilder builder) {
		return Collections.emptyList();
	}
}
