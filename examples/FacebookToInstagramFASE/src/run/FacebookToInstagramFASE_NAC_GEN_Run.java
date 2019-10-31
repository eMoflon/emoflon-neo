package run;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.API_FacebookToInstagramGrammar_NAC_GEN;
import org.emoflon.neo.api.API_Transformations;
import org.emoflon.neo.engine.generator.Generator;
import org.emoflon.neo.engine.modules.matchreprocessors.ParanoidNeoReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.AllRulesAllMatchesScheduler;
import org.emoflon.neo.engine.modules.terminationcondition.TimedTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.RandomSingleMatchUpdatePolicy;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

public class FacebookToInstagramFASE_NAC_GEN_Run {
	private static final Logger logger = Logger.getLogger(FacebookToInstagramFASE_NAC_GEN_Run.class);

	public static void main(String[] pArgs) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		
		var builder = API_Common.createBuilder();

		try {
			var api = new API_Transformations(builder);
			api.exportMetamodelsForFacebookToInstagramGrammar();
			var genAPI = new API_FacebookToInstagramGrammar_NAC_GEN(builder);
			
			Generator<NeoMatch, NeoCoMatch> generator = new Generator<NeoMatch, NeoCoMatch>(//
					genAPI.getAllRulesForFacebookToInstagramGrammarNAC__GEN(),
					new TimedTerminationCondition(3000), 
					new AllRulesAllMatchesScheduler(), //
					new RandomSingleMatchUpdatePolicy(), //
					new ParanoidNeoReprocessor(), //
					new HeartBeatAndReportMonitor());

			generator.generate();
			
			logger.info("Generation done.");
		} finally {
			builder.close();
		}
	}
}
