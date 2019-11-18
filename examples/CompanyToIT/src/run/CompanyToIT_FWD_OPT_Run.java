package run;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.API_CompanyToIT;
import org.emoflon.neo.api.API_CompanyToITTriplesForTesting;
import org.emoflon.neo.api.CompanyToIT.API_CompanyToIT_FWD_OPT;
import org.emoflon.neo.api.CompanyToIT.API_CompanyToIT_GEN;
import org.emoflon.neo.api.metamodels.API_Company;
import org.emoflon.neo.api.metamodels.API_IT;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.emsl.eMSL.Model;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.cleanup.NoOpCleanup;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
import org.emoflon.neo.engine.modules.matchreprocessors.FWD_OPTReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.FWD_OPTRuleScheduler;
import org.emoflon.neo.engine.modules.startup.NoOpStartup;
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
			var sourceModel = "Source";
			var targetModel = "Target";
			Model triple = new API_CompanyToITTriplesForTesting(builder).getModel_ConsistentTriple();
			builder.exportEMSLEntityToNeo4j(triple);
			builder.deleteAllCorrs();

			Collection<Long> elementIds = builder.getAllElementIDsInTriple("", targetModel);
			builder.deleteNodes(elementIds);
			builder.deleteEdges(elementIds);

			var genAPI = new API_CompanyToIT_GEN(builder);
			var fwdAPI = new API_CompanyToIT_FWD_OPT(builder);
			var tripleRules = new API_CompanyToIT(builder).getTripleRulesOfCompanyToIT();

			var forwardTransformation = new CorrCreationOperationalStrategy(solver, builder,
					genAPI.getAllRulesForCompanyToIT__GEN(), fwdAPI.getAllRulesForCompanyToIT__FWD_OPT(),
					getNegativeConstraints(builder), sourceModel, targetModel);
			var generator = new NeoGenerator(//
					fwdAPI.getAllRulesForCompanyToIT__FWD_OPT(), //
					new NoOpStartup(),// FIXME[Nils] Implement start up for OPT
					new NoMoreMatchesTerminationCondition(), //
					new FWD_OPTRuleScheduler(tripleRules), //
					forwardTransformation, //
					new FWD_OPTReprocessor(tripleRules), //
					new NoOpCleanup(), // FIXME [Nils] Implement clean up for OPT
					new HeartBeatAndReportMonitor(), //
					new ModelNameValueGenerator(sourceModel, targetModel), //
					List.of(new LoremIpsumStringValueGenerator()));

			logger.info("Start forward transformation...");
			generator.generate();

			if (forwardTransformation.isConsistent()) {
				logger.info("Your triple is consistent!");
				return true;
			} else {
				logger.info("Your triple is inconsistent!");
				var inconsistentElements = forwardTransformation.determineInconsistentElements();
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
