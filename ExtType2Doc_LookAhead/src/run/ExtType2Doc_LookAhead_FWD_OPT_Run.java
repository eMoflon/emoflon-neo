package run;

import static run.ExtType2Doc_LookAhead_GEN_Run.SRC_MODEL_NAME;
import static run.ExtType2Doc_LookAhead_GEN_Run.TRG_MODEL_NAME;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.exttype2doc_lookahead.API_Common;
import org.emoflon.neo.api.exttype2doc_lookahead.API_ExtType2Doc_LookAhead;
import org.emoflon.neo.api.exttype2doc_lookahead.tgg.API_ExtType2Doc_LookAhead_FWD_OPT;
import org.emoflon.neo.api.exttype2doc_lookahead.tgg.API_ExtType2Doc_LookAhead_GEN;
import org.emoflon.neo.api.exttype2doc_lookahead.metamodels.API_ExtDocModel;
import org.emoflon.neo.api.exttype2doc_lookahead.metamodels.API_ExtTypeModel;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
import org.emoflon.neo.engine.modules.matchreprocessors.FWD_OPTReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.FWD_OPTRuleScheduler;
import org.emoflon.neo.engine.modules.startup.NoOpStartup;
import org.emoflon.neo.engine.modules.terminationcondition.NoMoreMatchesTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.CorrCreationOperationalStrategy;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;
import org.emoflon.neo.engine.modules.analysis.*;

public class ExtType2Doc_LookAhead_FWD_OPT_Run {
	private static final Logger logger = Logger.getLogger(ExtType2Doc_LookAhead_FWD_OPT_Run.class);
	private static final SupportedILPSolver solver = SupportedILPSolver.Gurobi;

	private String srcModel = SRC_MODEL_NAME;
	private String trgModel = TRG_MODEL_NAME;
	private CorrCreationOperationalStrategy forwardTransformation;

	public static void main(String[] pArgs) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		var app = new ExtType2Doc_LookAhead_FWD_OPT_Run();
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
		var api = new API_ExtType2Doc_LookAhead(builder);
		var genAPI = new API_ExtType2Doc_LookAhead_GEN(builder);
		var fwd_optAPI = new API_ExtType2Doc_LookAhead_FWD_OPT(builder);
		var genRules = genAPI.getAllRulesForExtType2Doc_LookAhead_GEN();
		var tripleRules = new API_ExtType2Doc_LookAhead(builder).getTripleRulesOfExtType2Doc_LookAhead();
		var analyser = new TripleRuleAnalyser(tripleRules);
		
		forwardTransformation = new CorrCreationOperationalStrategy(//
				solver, //
				builder, //
				genRules, //
				fwd_optAPI.getAllRulesForExtType2Doc_LookAhead_FWD_OPT(), //
				api.getConstraintsOfExtType2Doc_LookAhead(), //
				srcModel, //
				trgModel//
		);

		return new NeoGenerator(//
				fwd_optAPI.getAllRulesForExtType2Doc_LookAhead_FWD_OPT(), //
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
}
