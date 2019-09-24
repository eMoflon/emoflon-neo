package run;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.API_CompanyToIT;
import org.emoflon.neo.api.CompanyToIT.API_CompanyToIT_CO;
import org.emoflon.neo.api.CompanyToIT.API_CompanyToIT_GEN;
import org.emoflon.neo.api.metamodels.API_Company;
import org.emoflon.neo.engine.generator.Generator;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
import org.emoflon.neo.engine.modules.matchreprocessors.NoOpReprocessor;
import org.emoflon.neo.engine.modules.monitors.SimpleLoggerMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.AllRulesAllMatchesScheduler;
import org.emoflon.neo.engine.modules.terminationcondition.OneShotTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.CheckOnlyOperationalStrategy;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

public class CompanyToIT_CO_Run {
	private static final Logger logger = Logger.getLogger(CompanyToIT_CO_Run.class);
	private static final SupportedILPSolver solver = SupportedILPSolver.Sat4J;

	public static void main(String[] pArgs) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);

		var builder = API_Common.createBuilder();

		try {
			var api = new API_CompanyToIT(builder);
			api.exportMetamodelsForCompanyToIT();

			var companyAPI = new API_Company(builder);
			var negativeConstraints = List.of(
					companyAPI.getConstraint_CEOOfMultipleCompanies()					
					, companyAPI.getConstraint_MultipleAdmins()
					);

			var genAPI = new API_CompanyToIT_GEN(builder);
			var checkOnly = new CheckOnlyOperationalStrategy(genAPI.getAllRules(), negativeConstraints);

			Generator<NeoMatch, NeoCoMatch> generator = new Generator<NeoMatch, NeoCoMatch>(//
					new OneShotTerminationCondition(), //
					new AllRulesAllMatchesScheduler(), //
					checkOnly, //
					new NoOpReprocessor(), //
					new SimpleLoggerMonitor());

			var coAPI = new API_CompanyToIT_CO(builder);
			generator.generate(coAPI.getAllRules());

			if (checkOnly.isConsistent(solver))
				logger.info("Your triple is consistent!");
			else {
				logger.info("Your triple is inconsistent!");
				
				logger.info("Now trying to determine inconsistent elements (this might take much longer):");
				var inconsistentElements = checkOnly.determineInconsistentElements(solver);
				logger.info(inconsistentElements.get().size() + " elements of your triple are inconsistent!");
			}
		} finally {
			builder.close();
		}
	}
}
