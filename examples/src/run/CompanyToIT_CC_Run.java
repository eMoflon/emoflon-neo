package run;

import static run.CompanyToIT_GEN_Run.SRC_MODEL_NAME;
import static run.CompanyToIT_GEN_Run.TRG_MODEL_NAME;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.companytoit.API_Common;
import org.emoflon.neo.api.companytoit.API_CompanyToIT;
import org.emoflon.neo.api.companytoit.tgg.API_CompanyToIT_CC;
import org.emoflon.neo.api.companytoit.tgg.API_CompanyToIT_GEN;
import org.emoflon.neo.api.companytoit.metamodels.API_Company;
import org.emoflon.neo.api.companytoit.metamodels.API_IT;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
import org.emoflon.neo.engine.modules.matchreprocessors.CCReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.CCRuleScheduler;
import org.emoflon.neo.engine.modules.startup.NoOpStartup;
import org.emoflon.neo.engine.modules.terminationcondition.NoMoreMatchesTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.CorrCreationOperationalStrategy;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;
import org.emoflon.neo.engine.modules.analysis.*;

public class CompanyToIT_CC_Run {
	private static final Logger logger = Logger.getLogger(CompanyToIT_CC_Run.class);
	private static final SupportedILPSolver solver = SupportedILPSolver.Gurobi;

	private String srcModel = SRC_MODEL_NAME;
	private String trgModel = TRG_MODEL_NAME;
	private CorrCreationOperationalStrategy corrCreation;

	public static void main(String[] pArgs) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		var app = new CompanyToIT_CC_Run();
		app.run();
	}

	public void run() throws Exception {
		try (var builder = API_Common.createBuilder()) {

			var generator = createGenerator(builder);

			logger.info("Start corr creation...");
			generator.generate();
		}
	}

	public NeoGenerator createGenerator(NeoCoreBuilder builder) {
		var api = new API_CompanyToIT(builder);
		var genAPI = new API_CompanyToIT_GEN(builder);
		var ccAPI = new API_CompanyToIT_CC(builder);
		var genRules = genAPI.getAllRulesForCompanyToIT_GEN();
		var tripleRules = new API_CompanyToIT(builder).getTripleRulesOfCompanyToIT();
		var analyser = new TripleRuleAnalyser(tripleRules);
		
		corrCreation = new CorrCreationOperationalStrategy(//
				solver, //
				builder, //
				genRules, //
				ccAPI.getAllRulesForCompanyToIT_CC(), //
				api.getConstraintsOfCompanyToIT(), //
				srcModel, //
				trgModel//
		);

		return new NeoGenerator(//
				ccAPI.getAllRulesForCompanyToIT_CC(), //
				new NoOpStartup(), //
				new NoMoreMatchesTerminationCondition(), //
				new CCRuleScheduler(analyser), //
				corrCreation, //
				new CCReprocessor(analyser), //
				corrCreation,//
				new HeartBeatAndReportMonitor(), //
				new ModelNameValueGenerator(srcModel, trgModel), //
				List.of(new LoremIpsumStringValueGenerator()));
	}

	public CorrCreationOperationalStrategy runCorrCreation(String srcModel, String trgModel) throws Exception {
		this.srcModel = srcModel;
		this.trgModel = trgModel;
		run();
		return corrCreation;
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