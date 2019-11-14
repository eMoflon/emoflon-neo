package run;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.API_Transformations;
import org.emoflon.neo.api.Transformations.API_FacebookToInstagramGrammar_CO;
import org.emoflon.neo.api.Transformations.API_FacebookToInstagramGrammar_GEN;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.cleanup.NoOpCleanup;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
import org.emoflon.neo.engine.modules.matchreprocessors.NoOpReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.AllRulesAllMatchesScheduler;
import org.emoflon.neo.engine.modules.startup.NoOpStartup;
import org.emoflon.neo.engine.modules.terminationcondition.OneShotTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.CheckOnlyOperationalStrategy;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;

public class FacebookToInstagramFASE_CO_Run {
	private static final Logger logger = Logger.getLogger(FacebookToInstagramFASE_CO_Run.class);
	private static final SupportedILPSolver solver = SupportedILPSolver.Gurobi;

	public static void main(String[] pArgs) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);

		var builder = API_Common.createBuilder();

		try {
			var api = new API_Transformations(builder);
			api.exportMetamodelsForFacebookToInstagramGrammar();

			Collection<IConstraint> negativeConstraints = List.of(//
					api.getConstraint_NoDoubleFollowership(), //
					api.getConstraint_NoDoubleFriendship()//
			);

			var genAPI = new API_FacebookToInstagramGrammar_GEN(builder);
			var coAPI = new API_FacebookToInstagramGrammar_CO(builder);

			var sourceModel = "Facebook";
			var targetModel = "Instagram";

			var checkOnly = new CheckOnlyOperationalStrategy(//
					genAPI.getAllRulesForFacebookToInstagramGrammar__GEN(), //
					coAPI.getAllRulesForFacebookToInstagramGrammar__CO(), //
					negativeConstraints, //
					builder, //
					sourceModel, //
					targetModel);

			var generator = new NeoGenerator(//
					coAPI.getAllRulesForFacebookToInstagramGrammar__CO(), //
					new NoOpStartup(), //
					new OneShotTerminationCondition(), //
					new AllRulesAllMatchesScheduler(), //
					checkOnly, //
					new NoOpReprocessor(), //
					new NoOpCleanup(), //
					new HeartBeatAndReportMonitor(), //
					new ModelNameValueGenerator(sourceModel, targetModel), //
					List.of(new LoremIpsumStringValueGenerator()));

			generator.generate();

			logger.info("Invoking ILP solver...");
			if (checkOnly.isConsistent(solver))
				logger.info("Your triple is consistent!");
			else {
				logger.info("Your triple is inconsistent!");
				var inconsistentElements = checkOnly.determineInconsistentElements(solver);
				logger.info(inconsistentElements.size() + " elements of your triple are inconsistent!");
			}
		} finally {
			builder.close();
		}
	}
}
