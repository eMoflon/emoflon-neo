package run;

import static run.JavaToDocSLE_GEN_Run.SRC_MODEL_NAME;
import static run.JavaToDocSLE_GEN_Run.TRG_MODEL_NAME;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.javatodocsle.API_Common;
import org.emoflon.neo.api.javatodocsle.API_JavaToDocSLE;
import org.emoflon.neo.api.javatodocsle.tgg.API_JavaToDocSLE_FWD_OPT;
import org.emoflon.neo.api.javatodocsle.tgg.API_JavaToDocSLE_GEN;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
import org.emoflon.neo.engine.modules.matchreprocessors.FWD_OPTReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.FWD_OPTRuleScheduler;
import org.emoflon.neo.engine.modules.startup.NoOpStartup;
import org.emoflon.neo.engine.modules.terminationcondition.NoMoreMatchesTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.CorrCreationOperationalStrategy;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;

public class JavaToDocSLE_FWD_OPT_Run {
	private static final Logger logger = Logger.getLogger(JavaToDocSLE_FWD_OPT_Run.class);
	private static final SupportedILPSolver solver = SupportedILPSolver.Gurobi;

	private String srcModel = SRC_MODEL_NAME;
	private String trgModel = TRG_MODEL_NAME;
	private CorrCreationOperationalStrategy forwardTransformation;

	public static void main(String[] pArgs) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		var app = new JavaToDocSLE_FWD_OPT_Run();
		app.run();
	}

	public void run() throws Exception {
		try (var builder = API_Common.createBuilder()) {

			var generator = createGenerator(builder);

			logger.info("Start forward transformation...");
			generator.generate();
		}
	}

	public NeoGenerator createGenerator(NeoCoreBuilder builder) {
		var genAPI = new API_JavaToDocSLE_GEN(builder);
		var fwd_optAPI = new API_JavaToDocSLE_FWD_OPT(builder);
		var genRules = genAPI.getAllRulesForJavaToDocSLE_GEN();
		var tripleRules = new API_JavaToDocSLE(builder).getTripleRulesOfJavaToDocSLE();
		var analyser = new TripleRuleAnalyser(tripleRules);
		var fwd_optRules = fwd_optAPI.getAllRulesForJavaToDocSLE_FWD_OPT();
		// remove ignore rules
		fwd_optRules.remove(fwd_optAPI.getRule_AddGlossaryRule().rule());
		fwd_optRules.remove(fwd_optAPI.getRule_AddGlossaryEntryRule().rule());
		fwd_optRules.remove(fwd_optAPI.getRule_LinkGlossaryEntryRule().rule());
				
		forwardTransformation = new CorrCreationOperationalStrategy(//
				solver, //
				builder, //
				genRules, //
				fwd_optRules, //
				getNegativeConstraints(builder), //
				srcModel, //
				trgModel//
		);

		return new NeoGenerator(//
				fwd_optAPI.getAllRulesForJavaToDocSLE_FWD_OPT(), //
				new NoOpStartup(), //
				new NoMoreMatchesTerminationCondition(), //
				new FWD_OPTRuleScheduler(analyser), //
				forwardTransformation, //
				new FWD_OPTReprocessor(analyser), //
				forwardTransformation,//
				new HeartBeatAndReportMonitor(), //
				new ModelNameValueGenerator(srcModel, trgModel), //
				List.of(new LoremIpsumStringValueGenerator()));
	}

	public CorrCreationOperationalStrategy runForwardTransformation(String srcModel, String trgModel) throws Exception {
		this.srcModel = srcModel;
		this.trgModel = trgModel;
		run();
		return forwardTransformation;
	}
	
	protected Collection<IConstraint> getNegativeConstraints(NeoCoreBuilder builder) {
		return Collections.emptyList();
	}
}
