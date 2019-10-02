package run.debug;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.ibex.tgg.ui.debug.api.Victory;
import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.API_CompanyToIT;
import org.emoflon.neo.api.CompanyToIT.API_CompanyToIT_GEN;
import org.emoflon.neo.engine.generator.Generator;
import org.emoflon.neo.engine.modules.matchreprocessors.ParanoidNeoReprocessor;
import org.emoflon.neo.engine.modules.monitors.SimpleLoggerMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.FixedNoOfMatchesRuleScheduler;
import org.emoflon.neo.engine.modules.terminationcondition.TimedTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.AnySingleMatchUpdatePolicy;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;
import org.emoflon.neo.victory.adapter.NeoVictoryAdapter;

public class CompanyToIT_DEBUG_GEN_Run {
	private static final Logger logger = Logger.getLogger(CompanyToIT_DEBUG_GEN_Run.class);

	public static void main(String[] pArgs) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);

		var builder = API_Common.createBuilder();

		try {
			var api = new API_CompanyToIT(builder);
			api.exportMetamodelsForCompanyToIT();

			var generator = new Generator<NeoMatch, NeoCoMatch>(//
					new TimedTerminationCondition(100000), //
					new FixedNoOfMatchesRuleScheduler(10), //
					new AnySingleMatchUpdatePolicy(), //
					new ParanoidNeoReprocessor(), //
					new SimpleLoggerMonitor());

			var genAPI = new API_CompanyToIT_GEN(builder);
			var adapter = new NeoVictoryAdapter(genAPI.getAllEMSLRulesForCompanyToIT__GEN(), api.getTripleRulesOfCompanyToIT());
			
			Victory.create(adapter);
			Victory.run();

			logger.info("Generation done.");
		} finally {
			builder.close();
		}
	}

}
