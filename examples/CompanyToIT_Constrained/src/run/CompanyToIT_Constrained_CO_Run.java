package run;

import static run.CompanyToIT_Constrained_GEN_Run.SRC_MODEL_NAME;
import static run.CompanyToIT_Constrained_GEN_Run.TRG_MODEL_NAME;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.companytoit_constrained.API_Common;
import org.emoflon.neo.api.companytoit_constrained.API_CompanyToIT_Constrained;
import org.emoflon.neo.api.companytoit_constrained.tgg.API_CompanyToIT_Constrained_CO;
import org.emoflon.neo.api.companytoit_constrained.tgg.API_CompanyToIT_Constrained_GEN;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
import org.emoflon.neo.engine.modules.matchreprocessors.NoOpReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.AllRulesAllMatchesScheduler;
import org.emoflon.neo.engine.modules.startup.NoOpStartup;
import org.emoflon.neo.engine.modules.terminationcondition.OneShotTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.CheckOnlyOperationalStrategy;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;

public class CompanyToIT_Constrained_CO_Run {
	private static final Logger logger = Logger.getLogger(CompanyToIT_Constrained_CO_Run.class);
	private static final SupportedILPSolver solver = SupportedILPSolver.Gurobi;

	private String srcModel = SRC_MODEL_NAME;
	private String trgModel = TRG_MODEL_NAME;
	private CheckOnlyOperationalStrategy checkOnly;

	public static void main(String[] pArgs) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		var app = new CompanyToIT_Constrained_CO_Run();
		app.run();
	}

	public void run() throws Exception {
		try (var builder = API_Common.createBuilder()) {

			var generator = createGenerator(builder);

			logger.info("Start check only...");
			generator.generate();
		}
	}

	public NeoGenerator createGenerator(NeoCoreBuilder builder) {
		var api = new API_CompanyToIT_Constrained(builder);
		var genAPI = new API_CompanyToIT_Constrained_GEN(builder);
		var coAPI = new API_CompanyToIT_Constrained_CO(builder);
		checkOnly = new CheckOnlyOperationalStrategy(//
				solver, //
				genAPI.getAllRulesForCompanyToIT_Constrained_GEN(), //
				coAPI.getAllRulesForCompanyToIT_Constrained_CO(), //
				api.getConstraintsOfCompanyToIT_Constrained(), //
				builder, //
				srcModel, //
				trgModel//
		);

		return new NeoGenerator(//
				coAPI.getAllRulesForCompanyToIT_Constrained_CO(), //
				new NoOpStartup(), //
				new OneShotTerminationCondition(), //
				new AllRulesAllMatchesScheduler(), //
				checkOnly, //
				new NoOpReprocessor(), //
				checkOnly, //
				new HeartBeatAndReportMonitor(), //
				new ModelNameValueGenerator(srcModel, trgModel), //
				List.of(new LoremIpsumStringValueGenerator()));
	}

	public CheckOnlyOperationalStrategy runCheckOnly(String srcModel, String trgModel) throws Exception {
		this.srcModel = srcModel;
		this.trgModel = trgModel;
		run();
		return checkOnly;
	}
}
