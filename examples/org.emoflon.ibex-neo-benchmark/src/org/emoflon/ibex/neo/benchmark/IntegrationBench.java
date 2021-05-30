package org.emoflon.ibex.neo.benchmark;

import static org.emoflon.neo.api.javatodocsle.run.JavaToDocSLE_GEN_Run.SRC_MODEL_NAME;
import static org.emoflon.neo.api.javatodocsle.run.JavaToDocSLE_GEN_Run.TRG_MODEL_NAME;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.emoflon.delta.validation.InvalidDeltaException;
import org.emoflon.ibex.neo.benchmark.util.BenchEntry;
import org.emoflon.ibex.neo.benchmark.util.BenchParameters;
import org.emoflon.neo.api.exttype2doc_concsync.run.ExtType2Doc_ConcSync_MI_Run;
import org.emoflon.neo.api.javatodocsle.API_Common;
import org.emoflon.neo.api.javatodocsle.API_JavaToDocSLE;
import org.emoflon.neo.api.javatodocsle.run.JavaToDocSLE_MI_Run;
import org.emoflon.neo.api.javatodocsle.tgg.API_JavaToDocSLE_GEN;
import org.emoflon.neo.api.javatodocsle.tgg.API_JavaToDocSLE_MI;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;
import org.emoflon.neo.engine.modules.ilp.ILPBasedOperationalStrategy;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
import org.emoflon.neo.engine.modules.matchreprocessors.MIReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.MIRuleScheduler;
import org.emoflon.neo.engine.modules.startup.PrepareContextDeltaAttributes;
import org.emoflon.neo.engine.modules.terminationcondition.NoMoreMatchesTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.*;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;

import delta.DeltaContainer;

public abstract class IntegrationBench<BP extends BenchParameters> extends NeoBench<ModelIntegrationOperationalStrategy, BP> {
	
	protected static final Logger logger = Logger.getLogger(IntegrationBench.class);
	
	public IntegrationBench(String projectName, String projectPath) {
		super(projectName, projectPath);
	}
	
	//public abstract void run(SupportedILPSolver solver) throws Exception;
	
	public void run(SupportedILPSolver solver) {
		try (var builder = API_Common.createBuilder()) {
	
			var generator = createGenerator(builder, solver);
	
			logger.info("Running generator...");
			generator.generate();
			logger.info("Generator terminated.");
		}
		catch (Exception e) {
			//TODO FixMe
		}
	}
	
	public NeoGenerator createGenerator(NeoCoreBuilder builder, SupportedILPSolver solver) {
		var api = new API_JavaToDocSLE(builder);
		var genAPI = new API_JavaToDocSLE_GEN(builder);
		var miAPI = new API_JavaToDocSLE_MI(builder);
		var genRules = genAPI.getAllRulesForJavaToDocSLE_GEN();
		var analyser = new TripleRuleAnalyser(new API_JavaToDocSLE(builder).getTripleRulesOfJavaToDocSLE());
		var modelIntegration = new ModelIntegrationOperationalStrategy(//
				solver, //
				builder, //
				genRules, //
				miAPI.getAllRulesForJavaToDocSLE_MI(), //
				api.getConstraintsOfJavaToDocSLE(), //
				source.toString(), //
				target.toString()//
		);
		
		return new NeoGenerator(//
				miAPI.getAllRulesForJavaToDocSLE_MI(), //
				new PrepareContextDeltaAttributes(builder, source.toString(), target.toString()), //
				new NoMoreMatchesTerminationCondition(), //
				new MIRuleScheduler(analyser), //
				modelIntegration, //
				new MIReprocessor(analyser), //
				modelIntegration, //
				new HeartBeatAndReportMonitor(), //
				new ModelNameValueGenerator(source.toString(), target.toString()), //
				List.of(new LoremIpsumStringValueGenerator()));
	}
	
	@Override
	protected BenchEntry applyDeltaAndRun(ILPBasedOperationalStrategy opStrat, BP parameters, boolean saveTransformedModels) throws IOException, InvalidDeltaException {
		long tic = System.currentTimeMillis();
		run(SupportedILPSolver.Gurobi);
		long toc = System.currentTimeMillis();
		double init = (double) (toc - tic) / 1000;

		DeltaContainer deltaContainer = (DeltaContainer) delta.getContents().get(0);
		applyDelta(deltaContainer);

		tic = System.currentTimeMillis();
		run(SupportedILPSolver.Gurobi);
		toc = System.currentTimeMillis();
		double resolve = (double) (toc - tic) / 1000;

		if (saveTransformedModels)
			saveModels();

		return new BenchEntry(parameters.modelScale, parameters.numOfChanges, numOfElements, init, resolve);
	}

	private void applyDelta(DeltaContainer dc) {
		
		
	}

}
