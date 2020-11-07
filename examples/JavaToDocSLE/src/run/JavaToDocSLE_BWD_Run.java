package run;

import static run.JavaToDocSLE_GEN_Run.SRC_MODEL_NAME;
import static run.JavaToDocSLE_GEN_Run.TRG_MODEL_NAME;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.javatodocsle.API_Common;
import org.emoflon.neo.api.javatodocsle.API_JavaToDocSLE;
import org.emoflon.neo.api.javatodocsle.tgg.API_JavaToDocSLE_BWD;
import org.emoflon.neo.emsl.util.FlattenerException;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.cleanup.RemoveTranslateAttributes;
import org.emoflon.neo.engine.modules.matchreprocessors.ParanoidNeoReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.AllRulesAllMatchesScheduler;
import org.emoflon.neo.engine.modules.startup.PrepareTranslateAttributes;
import org.emoflon.neo.engine.modules.terminationcondition.NoMoreMatchesTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.AnySingleMatchUpdatePolicy;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;

public class JavaToDocSLE_BWD_Run {
	private static final Logger logger = Logger.getLogger(JavaToDocSLE_BWD_Run.class);

	public static void main(String[] pArgs) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		var app = new JavaToDocSLE_BWD_Run();
		app.runGenerator();
	}

	public void runGenerator() throws FlattenerException, Exception {
		try (var builder = API_Common.createBuilder()) {
			new API_JavaToDocSLE(builder).exportMetamodelsForJavaToDocSLE();

			var generator = new NeoGenerator(//
					new API_JavaToDocSLE_BWD(builder).getAllRulesForJavaToDocSLE_BWD(), //
					new PrepareTranslateAttributes(builder, TRG_MODEL_NAME), //
					new NoMoreMatchesTerminationCondition(), //
					new AllRulesAllMatchesScheduler(), //
					new AnySingleMatchUpdatePolicy(), //
					new ParanoidNeoReprocessor(), //
					new RemoveTranslateAttributes(builder, TRG_MODEL_NAME), //
					new HeartBeatAndReportMonitor(), //
					new ModelNameValueGenerator(SRC_MODEL_NAME, TRG_MODEL_NAME), //
					List.of(new LoremIpsumStringValueGenerator()));

			logger.info("Start model generation...");
			generator.generate();
			logger.info("Generation done.");
		}
	}
}
