package run;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.CompanyToIT.API_CompanyToIT_CC;
import org.emoflon.neo.api.CompanyToIT.API_CompanyToIT_GEN;
import org.emoflon.neo.api.metamodels.API_Company;
import org.emoflon.neo.api.metamodels.API_IT;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.cypher.rules.NeoRule;
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

public class CompanyToIT_CC_Run {
	private static final Logger logger = Logger.getLogger(CompanyToIT_CC_Run.class);
	private static final SupportedILPSolver solver = SupportedILPSolver.Gurobi;

	public static void main(String[] pArgs) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		Logger.getLogger(NeoRule.class).setLevel(Level.DEBUG);
		var app = new CompanyToIT_CC_Run();
		app.runCorrCreation();
	}

	public boolean runCorrCreation() throws Exception {
		try (var builder = API_Common.createBuilder()) {
			var genAPI = new API_CompanyToIT_GEN(builder);
			var ccAPI = new API_CompanyToIT_CC(builder);
			var sourceModel = "TheSource";
			var targetModel = "TheTarget";
			var corrCreation = new CorrCreationOperationalStrategy(builder, genAPI.getAllRulesForCompanyToIT__GEN(),
					ccAPI.getAllRulesForCompanyToIT__CC(), getNegativeConstraints(builder), sourceModel, targetModel);
			var generator = new NeoGenerator(//
					ccAPI.getAllRulesForCompanyToIT__CC(), //
					new NoMoreMatchesTerminationCondition(), //
					new NewCorrRuleScheduler(), //
					corrCreation, //
					new ParanoidNeoReprocessor(), //
					new HeartBeatAndReportMonitor(), //
					new ModelNameValueGenerator(sourceModel, targetModel), //
					List.of(new LoremIpsumStringValueGenerator()));

			logger.info("Start corr creation...");
			generator.generate();

			if (corrCreation.isConsistent(solver)) {
				logger.info("Your triple is consistent!");
				return true;
			} else {
				logger.info("Your triple is inconsistent!");
				var inconsistentElements = corrCreation.determineInconsistentElements(solver);
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
