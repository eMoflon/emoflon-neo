package run.debug;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.API_CompanyToIT;
import org.emoflon.neo.api.CompanyToIT.API_CompanyToIT_GEN;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.cleanup.NoOpCleanup;
import org.emoflon.neo.engine.modules.matchreprocessors.ParanoidNeoReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.AllRulesAllMatchesScheduler;
import org.emoflon.neo.engine.modules.startup.NoOpStartup;
import org.emoflon.neo.engine.modules.terminationcondition.NoTerminationCondition;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;
import org.emoflon.neo.victory.adapter.NeoVictoryAdapter;

public class CompanyToIT_DEBUG_GEN_Run {
	private static final Logger logger = Logger.getLogger(CompanyToIT_DEBUG_GEN_Run.class);

	public static void main(String[] pArgs) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);

		var builder = API_Common.createBuilder();

		try {
			var api = new API_CompanyToIT(builder);
			api.exportMetamodelsForCompanyToIT();

			var genAPI = new API_CompanyToIT_GEN(builder);
			var allRules = genAPI.getAllRulesForCompanyToIT__GEN();
			var adapter = new NeoVictoryAdapter(builder, genAPI.getAllEMSLRulesForCompanyToIT__GEN(), //
					api.getTripleRulesOfCompanyToIT());

			var generator = new NeoGenerator(//
					allRules, //
					new NoOpStartup(), //
					new NoTerminationCondition(), //
					new AllRulesAllMatchesScheduler(), //
					adapter, //
					new ParanoidNeoReprocessor(), //
					new NoOpCleanup(), //
					new HeartBeatAndReportMonitor(), //
					new ModelNameValueGenerator("TheSource", "TheTarget"), //
					List.of(new LoremIpsumStringValueGenerator()));

			adapter.run(generator);

			logger.info("Generation done.");
		} finally {
			builder.close();
		}
	}

}
