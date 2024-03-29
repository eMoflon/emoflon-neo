package org.emoflon.neo.example.javatodoc.performance;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.javatodocsle.API_Common;
import org.emoflon.neo.api.javatodocsle.API_JavaToDocSLE;
import org.emoflon.neo.api.javatodocsle.tgg.API_JavaToDocSLE_MI;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.modules.NeoGenerator;

import static org.emoflon.neo.api.javatodocsle.run.JavaToDocSLE_GEN_Run.SRC_MODEL_NAME;
import static org.emoflon.neo.api.javatodocsle.run.JavaToDocSLE_GEN_Run.TRG_MODEL_NAME;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.api.javatodocsle.tgg.API_JavaToDocSLE_GEN;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.startup.NoOpStartup;
import org.emoflon.neo.engine.modules.terminationcondition.NoMoreMatchesTerminationCondition;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;
import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;
import org.emoflon.neo.engine.modules.updatepolicies.ModelIntegrationOperationalStrategy;
import org.emoflon.neo.engine.modules.startup.PrepareContextDeltaAttributes;
import org.emoflon.neo.engine.modules.ruleschedulers.MIRuleScheduler;
import org.emoflon.neo.engine.modules.matchreprocessors.MIReprocessor;

@SuppressWarnings("unused")
public class JavaToDocSLE_MI_Run {
	private ModelIntegrationOperationalStrategy modelIntegration;
	protected static final Logger logger = Logger.getLogger(JavaToDocSLE_MI_Run.class);
	protected String srcModelName;
	protected String trgModelName;

	public static void main(String[] args) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		var app = new JavaToDocSLE_MI_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		app.run(SupportedILPSolver.Gurobi);
	}

	public JavaToDocSLE_MI_Run(String srcModelName, String trgModelName) {
		this.srcModelName = srcModelName;
		this.trgModelName = trgModelName;
	}

	public void run(SupportedILPSolver solver) throws Exception {
		try (var builder = API_Common.createBuilder()) {

			var generator = createGenerator(builder, solver);

			logger.info("Running generator...");
			generator.generate();
			logger.info("Generator terminated.");
		}
	}

	public NeoGenerator createGenerator(NeoCoreBuilder builder, SupportedILPSolver solver) {
		var api = new API_JavaToDocSLE(builder);
		var genAPI = new API_JavaToDocSLE_GEN(builder);
		var miAPI = new API_JavaToDocSLE_MI(builder);
		var genRules = genAPI.getAllRulesForJavaToDocSLE_GEN();
		var analyser = new TripleRuleAnalyser(new API_JavaToDocSLE(builder).getTripleRulesOfJavaToDocSLE());
		
		modelIntegration = new ModelIntegrationOperationalStrategy(//
				solver, //
				builder, //
				genRules, //
				miAPI.getAllRulesForJavaToDocSLE_MI(), //
				api.getConstraintsOfJavaToDocSLE(), //
				srcModelName, //
				trgModelName//
		);

		return new NeoGenerator(//
				miAPI.getAllRulesForJavaToDocSLE_MI(), //
				new PrepareContextDeltaAttributes(builder, srcModelName, trgModelName), //
				new NoMoreMatchesTerminationCondition(), //
				new MIRuleScheduler(analyser), //
				modelIntegration, //
				new MIReprocessor(analyser), //
				modelIntegration, //
				new HeartBeatAndReportMonitor(), //
				new ModelNameValueGenerator(srcModelName, trgModelName), //
				List.of(new LoremIpsumStringValueGenerator()));
	}

	public ModelIntegrationOperationalStrategy runModelIntegration(SupportedILPSolver solver) throws Exception {
		run(solver);
		return modelIntegration;
	}
}
