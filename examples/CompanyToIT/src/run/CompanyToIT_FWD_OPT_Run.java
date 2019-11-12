package run;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.CompanyToIT.API_CompanyToIT_CC;
import org.emoflon.neo.api.CompanyToIT.API_CompanyToIT_FWD;
import org.emoflon.neo.api.CompanyToIT.API_CompanyToIT_GEN;
import org.emoflon.neo.api.metamodels.API_Company;
import org.emoflon.neo.api.metamodels.API_IT;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
import org.emoflon.neo.engine.modules.matchreprocessors.ParanoidNeoReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.NewCorrRuleScheduler;
import org.emoflon.neo.engine.modules.terminationcondition.NoMoreMatchesTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.CorrCreationOperationalStrategy;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;

public class CompanyToIT_FWD_OPT_Run {
	private static final Logger logger = Logger.getLogger(CompanyToIT_FWD_OPT_Run.class);
	private static final SupportedILPSolver solver = SupportedILPSolver.Gurobi;

	public static void main(String[] pArgs) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		var app = new CompanyToIT_FWD_OPT_Run();
		app.runForwardTransformation();
	}

	public boolean runForwardTransformation() throws Exception {
		try (var builder = API_Common.createBuilder()) {
			var genAPI = new API_CompanyToIT_GEN(builder);
			var fwdAPI = new API_CompanyToIT_FWD(builder);
			var forwardTransformation = new CorrCreationOperationalStrategy(builder, genAPI.getAllRulesForCompanyToIT__GEN(),
					fwdAPI.getAllRulesForCompanyToIT__FWD(), getNegativeConstraints(builder));
			var generator = new NeoGenerator(//
					fwdAPI.getAllRulesForCompanyToIT__FWD(), //
					new NoMoreMatchesTerminationCondition(), //
					new NewCorrRuleScheduler(), //
					forwardTransformation, //
					new ParanoidNeoReprocessor(), //
					new HeartBeatAndReportMonitor(), //
					new ModelNameValueGenerator("TheSource", "TheTarget"), //
					List.of(new LoremIpsumStringValueGenerator()));

			logger.info("Start corr creation...");
			generator.generate();

			if (forwardTransformation.isConsistent(solver)) {
				logger.info("Your triple is consistent!");
				return true;
			} else {
				logger.info("Your triple is inconsistent!");
				var inconsistentElements = forwardTransformation.determineInconsistentElements(solver);
				logger.info(inconsistentElements + " elements of your triple are inconsistent!");
				return false;
			}
		}
	}

	protected Collection<IConstraint> getNegativeConstraints(NeoCoreBuilder builder) {
		var companyAPI = new API_Company(builder);
		var itAPI = new API_IT(builder);
		return List.of(//
				companyAPI.getConstraint_CEOOfMultipleCompanies(), //
				companyAPI.getConstraint_MultipleAdmins(), //
				itAPI.getConstraint_NoDifferentITThanRouter()//
		);
	}
}
