package org.emoflon.ibex.neo.benchmark.exttype2doc.shortCut;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.emoflon.ibex.neo.benchmark.SynchronizationBench;
import org.emoflon.ibex.neo.benchmark.exttype2doc.concsync.ExtType2Doc_ConcSync_Bench;
import org.emoflon.ibex.neo.benchmark.exttype2doc.concsync.ExtType2Doc_ConcSync_Params;
import org.emoflon.ibex.neo.benchmark.util.BenchEntry;
import org.emoflon.ibex.neo.benchmark.util.ScaleOrientation;
import org.emoflon.neo.api.exttype2doc_shortcut.API_Common;
import org.emoflon.neo.api.exttype2doc_shortcut.run.API_ConflictGenerator;
import org.emoflon.neo.api.exttype2doc_shortcut.API_ExtType2Doc_ShortCut;
import org.emoflon.neo.api.exttype2doc_shortcut.tgg.API_ExtType2Doc_ShortCut_GEN;
import org.emoflon.neo.api.exttype2doc_shortcut.tgg.API_ExtType2Doc_ShortCut_MI;
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

public class ExtType2Doc_ShortCut_Bench extends SynchronizationBench<ExtType2Doc_ShortCut_Params> {
	
	protected API_ConflictGenerator api;
	
	public ExtType2Doc_ShortCut_Bench(String projectName, String pathName) {
		super(projectName, pathName);
		builder = API_Common.createBuilder();
		api = new API_ConflictGenerator(builder);
	}

	@Override
	protected void applyDelta(ExtType2Doc_ShortCut_Params parameters) {
		
		for (int i=0; i < parameters.num_of_conflicts; i +=5 /* as there are five conflicts per iteration*/) {
			api.getRule_CreatePackageRoot().rule().apply();
			api.getRule_CreateTypeRoot().rule().apply();
			api.getRule_MovePackage().rule().apply();
			api.getRule_MoveTypeLeaf().rule().apply();
			api.getRule_MoveTypeRoot().rule().apply();
		}
		
	}

	@Override
	public ModelIntegrationOperationalStrategy initOpStrat(NeoCoreBuilder builder, SupportedILPSolver solver) {
		var miAPI = new API_ExtType2Doc_ShortCut_MI(builder);
		var genAPI = new API_ExtType2Doc_ShortCut_GEN(builder);
		var genRules = genAPI.getAllRulesForExtType2Doc_ShortCut_GEN();
		
		return new ModelIntegrationOperationalStrategy(//
				solver, //
				builder, //
				genRules, //
				miAPI.getAllRulesForExtType2Doc_ShortCut_MI(), //
				getNegativeConstraints(builder), //
				filename_src, //
				filename_trg//
		);
	}

	@Override
	public NeoGenerator createGenerator(NeoCoreBuilder builder, SupportedILPSolver solver) {
		var miAPI = new API_ExtType2Doc_ShortCut_MI(builder);
		var tripleRules = new API_ExtType2Doc_ShortCut(builder).getTripleRulesOfExtType2Doc_ShortCut();
		var analyser = new TripleRuleAnalyser(tripleRules);
		var modelIntegration = initOpStrat(builder, solver);
		
		return new NeoGenerator(//
				miAPI.getAllRulesForExtType2Doc_ShortCut_MI(), //
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

	public static void main(String[] args) {
		ExtType2Doc_ShortCut_Bench bench = new ExtType2Doc_ShortCut_Bench("../ExtType2Doc_ShortCut/emf/", "../ExtType2Doc_ShortCut/emf/");

		ExtType2Doc_ShortCut_Params params = new ExtType2Doc_ShortCut_Params( //
				args[0], // name
				Integer.valueOf(args[1]), // model scale
				ScaleOrientation.valueOf(args[2].contains("H") ? "HORIZONTAL" : "VERTICAL"), // scale orientation
				Integer.valueOf(args[3]), // number of changes
				args[4]
		);

		BenchEntry<ExtType2Doc_ShortCut_Params> result = bench.genAndBench(params);
		System.out.println(result);
	}
}
