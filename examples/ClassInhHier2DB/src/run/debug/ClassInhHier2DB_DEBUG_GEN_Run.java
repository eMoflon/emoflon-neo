package run.debug;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.classinhhier2db.API_Common;
import org.emoflon.neo.api.classinhhier2db.API_ClassInhHier2DB;
import org.emoflon.neo.api.classinhhier2db.tgg.API_ClassInhHier2DB_GEN;
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

import run.ClassInhHier2DB_GEN_Run;

public class ClassInhHier2DB_DEBUG_GEN_Run {
	private static final Logger logger = Logger.getLogger(ClassInhHier2DB_DEBUG_GEN_Run.class);

	public static void main(String[] pArgs) throws Exception {
		Logger.getRootLogger().setLevel(Level.DEBUG);

		var builder = API_Common.createBuilder();
		var srcModel = ClassInhHier2DB_GEN_Run.SRC_MODEL_NAME;
		var trgModel = ClassInhHier2DB_GEN_Run.TRG_MODEL_NAME;

		try {
			var api = new API_ClassInhHier2DB(builder);
			api.exportMetamodelsForClassInhHier2DB();

			var genAPI = new API_ClassInhHier2DB_GEN(builder);
			var allRules = genAPI.getAllRulesForClassInhHier2DB_GEN();
			var adapter = new NeoVictoryAdapter(//
					builder, //
					genAPI.getAllEMSLRulesForClassInhHier2DB_GEN(), //
					api.getTripleRulesOfClassInhHier2DB(), //
					srcModel, trgModel//
			);

			var generator = new NeoGenerator(//
					allRules, //
					new NoOpStartup(), //
					new NoTerminationCondition(), //
					new AllRulesAllMatchesScheduler(), //
					adapter, //
					new ParanoidNeoReprocessor(), //
					new NoOpCleanup(), //
					new HeartBeatAndReportMonitor(), //
					new ModelNameValueGenerator(srcModel, trgModel), //
					List.of(new LoremIpsumStringValueGenerator()));

			adapter.run(generator);

			logger.info("Generation done.");
		} finally {
			builder.close();
		}
	}

}
