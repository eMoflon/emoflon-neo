package run;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.API_Facebook;
import org.emoflon.neo.api.API_Instagram;
import org.emoflon.neo.api.Transformations.API_FacebookToInstagramGrammar_CO;
import org.emoflon.neo.api.Transformations.API_FacebookToInstagramGrammar_GEN;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
import org.emoflon.neo.engine.modules.matchreprocessors.NoOpReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.AllRulesAllMatchesScheduler;
import org.emoflon.neo.engine.modules.terminationcondition.OneShotTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.CheckOnlyOperationalStrategy;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;

public class FacebookToInstagram_CO_Run {
	private static final Logger logger = Logger.getLogger(FacebookToInstagram_CO_Run.class);
	private static final SupportedILPSolver solver = SupportedILPSolver.Gurobi;

	public static void main(String[] pArgs) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		var app = new FacebookToInstagram_CO_Run();
		app.runCheckOnly();
	}

	public boolean runCheckOnly() throws Exception {
		try (var builder = API_Common.createBuilder()) {
			var genAPI = new API_FacebookToInstagramGrammar_GEN(builder);
			var checkOnly = new CheckOnlyOperationalStrategy(genAPI.getAllRulesForFacebookToInstagramGrammar__GEN(),
					getNegativeConstraints(builder));

			var coAPI = new API_FacebookToInstagramGrammar_CO(builder);
			var generator = new NeoGenerator(//
					coAPI.getAllRulesForFacebookToInstagramGrammar__CO(), //
					new OneShotTerminationCondition(), //
					new AllRulesAllMatchesScheduler(), //
					checkOnly, //
					new NoOpReprocessor(), //
					new HeartBeatAndReportMonitor(), //
					new ModelNameValueGenerator("Facebook", "Instagram"), //
					List.of(new LoremIpsumStringValueGenerator()));

			logger.info("Start check only...");
			generator.generate();

			if (checkOnly.isConsistent(solver)) {
				logger.info("Your triple is consistent!");
				return true;
			} else {
				logger.info("Your triple is inconsistent!");
				var inconsistentElements = checkOnly.determineInconsistentElements(solver);
				logger.info(inconsistentElements.get().size() + " elements of your triple are inconsistent!");
				return false;
			}
		}
	}

	protected Collection<IConstraint> getNegativeConstraints(NeoCoreBuilder builder) {
		var fb = new API_Facebook(builder);
		var inst = new API_Instagram(builder);
		return List.of(//
				fb.getConstraint_NoDoubleFaceBookUsers(), //
				fb.getConstraint_NoDoubleFriendship(), //
				fb.getConstraint_NoInterFriendship(), //
				fb.getConstraint_NoDoubleParents(), //
				fb.getConstraint_NoDoubleSibling(), //
				fb.getConstraint_NoDoubleSpouses(), //
				inst.getConstraint_NoDoubleFollowership(), //
				inst.getConstraint_NoDoubleInstagramUsers());
	}
}
