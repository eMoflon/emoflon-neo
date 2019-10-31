package run;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.API_FacebookToInstagramGrammar_NAC_GEN;
import org.emoflon.neo.api.API_Transformations;
import org.emoflon.neo.api.Transformations.API_FacebookToInstagramGrammar_CO;
import org.emoflon.neo.api.Transformations.API_FacebookToInstagramGrammar_GEN;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.engine.generator.Generator;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
import org.emoflon.neo.engine.modules.matchreprocessors.NoOpReprocessor;
import org.emoflon.neo.engine.modules.matchreprocessors.ParanoidNeoReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.AllRulesAllMatchesScheduler;
import org.emoflon.neo.engine.modules.terminationcondition.CounterTerminationCondition;
import org.emoflon.neo.engine.modules.terminationcondition.OneShotTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.CheckOnlyOperationalStrategy;
import org.emoflon.neo.engine.modules.updatepolicies.RandomSingleMatchUpdatePolicy;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

public class FacebookToInstagramFASE_Eval_Run {

	private static final Logger logger = Logger.getLogger(FacebookToInstagramFASE_CO_Run.class);
	private static final SupportedILPSolver solver = SupportedILPSolver.Gurobi;
	private static final int nrOfIterations = 10;

	public static void main(String[] pArgs) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		var builder = API_Common.createBuilder();
		
		int[] modelsizes = { /*1000, 2000, 5000, 10000, */20000/*, 50000*/ };
		//int[] modelsizes = { 100, 200 };
		//int previousSize = 0;

		try {
			
			for (int size : modelsizes) {
				var api = new API_Transformations(builder);
				api.exportMetamodelsForFacebookToInstagramGrammar();
				//var nacAPI = new API_FacebookToInstagramGrammar_NAC_GEN(builder);
				var genAPI = new API_FacebookToInstagramGrammar_GEN(builder);

				Generator<NeoMatch, NeoCoMatch> generator;
				/* = new Generator<NeoMatch, NeoCoMatch>(//
						nacAPI.getAllRulesForFacebookToInstagramGrammarNAC__GEN(), //
						new CounterTerminationCondition(size - previousSize), //
						new AllRulesAllMatchesScheduler(), //
						new RandomSingleMatchUpdatePolicy(), //
						new ParanoidNeoReprocessor(), //
						new HeartBeatAndReportMonitor());

				generator.generate();

				logger.info("Generation done for " + size + " elements.");
				previousSize = size;*/

				for (int i = 0; i < nrOfIterations; i++) {
					var coAPI = new API_FacebookToInstagramGrammar_CO(builder);
					
//					Collection<IConstraint> negativeConstraints = List.of(//
//							api.getConstraint_NoDoubleFollowership(), //
//							api.getConstraint_NoDoubleFriendship()//
//					);
					Collection<IConstraint> negativeConstraints = Collections.emptyList();
					
					var checkOnly = new CheckOnlyOperationalStrategy(genAPI.getAllRulesForFacebookToInstagramGrammar__GEN(),
							negativeConstraints);
					
					logger.info("Start consistency check " + (i+1) + "/" + nrOfIterations + " for " + size + " elements...");
					generator = new Generator<NeoMatch, NeoCoMatch>(//
							coAPI.getAllRulesForFacebookToInstagramGrammar__CO(), //
							new OneShotTerminationCondition(), //
							new AllRulesAllMatchesScheduler(), //
							checkOnly, //
							new NoOpReprocessor(), //
							new HeartBeatAndReportMonitor());

					generator.generate();

					logger.info("Invoking ILP solver...");
					if (checkOnly.isConsistent(solver))
						logger.info("Your triple is consistent!");
					else {
						logger.info("Your triple is inconsistent!");
						var inconsistentElements = checkOnly.determineInconsistentElements(solver);
						logger.info(inconsistentElements.get().size() + " elements of your triple are inconsistent!");
					}
					// logger.info("Number of negative constraints: " + checkOnly.getNroOfGraphConstraints());
					// logger.info("Number of linear constraints: " + checkOnly.getNrOfILPConstraints());
					logger.info(checkOnly.getInfo());
				}
			}
		} finally {
			builder.close();
		}
	}
}
