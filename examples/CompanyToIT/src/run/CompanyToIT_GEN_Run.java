package run;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.API_CompanyToIT;
import org.emoflon.neo.api.CompanyToIT.API_CompanyToIT_GEN;
import org.emoflon.neo.emsl.util.FlattenerException;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.matchreprocessors.ParanoidNeoReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.AllRulesAllMatchesScheduler;
import org.emoflon.neo.engine.modules.terminationcondition.TimedTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.RandomSingleMatchUpdatePolicy;

public class CompanyToIT_GEN_Run {
	private static final Logger logger = Logger.getLogger(CompanyToIT_GEN_Run.class);

	public static void main(String[] pArgs) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		var app = new CompanyToIT_GEN_Run();
		app.runGenerator();
	}

	public void runGenerator() throws FlattenerException, Exception {
		try (var builder = API_Common.createBuilder()) {
			var api = new API_CompanyToIT(builder);
			api.exportMetamodelsForCompanyToIT();

			var genAPI = new API_CompanyToIT_GEN(builder);
			var generator = createGenerator(genAPI);

			logger.info("Start model generation...");
			generator.generate();
			logger.info("Generation done.");
		}
	}

	protected NeoGenerator createGenerator(API_CompanyToIT_GEN genAPI) {
		var allRules = genAPI.getAllRulesForCompanyToIT__GEN();

		return new NeoGenerator(//
				allRules, //
				new TimedTerminationCondition(30000), //
				new AllRulesAllMatchesScheduler(), //
				new RandomSingleMatchUpdatePolicy(), //
				new ParanoidNeoReprocessor(), //
				new HeartBeatAndReportMonitor());
	}
}
