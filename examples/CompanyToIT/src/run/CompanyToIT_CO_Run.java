package run;

import static run.CompanyToIT_GEN_Run.SRC_MODEL_NAME;
import static run.CompanyToIT_GEN_Run.TRG_MODEL_NAME;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.API_CompanyToIT;
import org.emoflon.neo.api.CompanyToIT.API_CompanyToIT_CO;
import org.emoflon.neo.api.CompanyToIT.API_CompanyToIT_GEN;
import org.emoflon.neo.api.metamodels.API_Company;
import org.emoflon.neo.api.metamodels.API_IT;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.api.constraints.IConstraint;
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

public class CompanyToIT_CO_Run {
	private static final Logger logger = Logger.getLogger(CompanyToIT_CO_Run.class);
	private static final SupportedILPSolver solver = SupportedILPSolver.Gurobi;

	private String srcModel = SRC_MODEL_NAME;
	private String trgModel = TRG_MODEL_NAME;
	private CheckOnlyOperationalStrategy checkOnly;

	public static void main(String[] pArgs) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		var app = new CompanyToIT_CO_Run();
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
		var api = new API_CompanyToIT(builder);
		var genAPI = new API_CompanyToIT_GEN(builder);
		var coAPI = new API_CompanyToIT_CO(builder);
		checkOnly = new CheckOnlyOperationalStrategy(//
				solver, //
				genAPI.getAllRulesForCompanyToIT_GEN(), //
				coAPI.getAllRulesForCompanyToIT_CO(), //
				api.getConstraintsOfCompanyToIT(), //
				builder, //
				srcModel, //
				trgModel//
		);

		return new NeoGenerator(//
				coAPI.getAllRulesForCompanyToIT_CO(), //
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

//	protected Collection<IConstraint> getNegativeConstraints(NeoCoreBuilder builder) {
//		var companyAPI = new API_Company(builder);
//		var itAPI = new API_IT(builder);
//		return List.of(//
//				companyAPI.getConstraint_CEOOfMultipleCompanies(), //
//				companyAPI.getConstraint_MultipleAdmins(), //
//				itAPI.getConstraint_NoDifferentITThanRouter()//
//		);
//	}
}
