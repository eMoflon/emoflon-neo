package run;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.exttype2doc_shortcut.API_Common;
import org.emoflon.neo.api.exttype2doc_shortcut.API_ExtType2Doc_ShortCut;
import org.emoflon.neo.api.exttype2doc_shortcut.tgg.*;
import org.emoflon.neo.api.exttype2doc_shortcut.metamodels.API_ExtTypeModel;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.emsl.util.FlattenerException;
import org.emoflon.neo.engine.generator.INodeSampler;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.cleanup.NoOpCleanup;
import org.emoflon.neo.engine.modules.matchreprocessors.ParanoidNeoReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.TwoPhaseRuleSchedulerForGEN;
import org.emoflon.neo.engine.modules.startup.NoOpStartup;
import org.emoflon.neo.engine.modules.terminationcondition.CompositeTerminationConditionForGEN;
import org.emoflon.neo.engine.modules.terminationcondition.MaximalRuleApplicationsTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.TwoPhaseUpdatePolicyForGEN;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;

public class ExtType2Doc_ShortCut_GEN_Run {
	public static final String SRC_MODEL_NAME = "ExtType2Doc_ShortCut_Source";
	public static final String TRG_MODEL_NAME = "ExtType2Doc_ShortCut_Target";

	private static final Logger logger = Logger.getLogger(ExtType2Doc_ShortCut_GEN_Run.class);

	public static void main(String[] pArgs) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		var app = new ExtType2Doc_ShortCut_GEN_Run();
		app.runGenerator();
	}

	public void runGenerator() throws FlattenerException, Exception {
		try (var builder = API_Common.createBuilder()) {
			new API_ExtType2Doc_ShortCut(builder).exportMetamodelsForExtType2Doc_ShortCut();

			var generator = createGenerator(builder);

			logger.info("Start model generation...");
			generator.generate();
			logger.info("Generation done.");
		}
	}

	protected NeoGenerator createGenerator(NeoCoreBuilder builder) {
		var allRules = new API_ExtType2Doc_ShortCut_GEN(builder).getAllRulesForExtType2Doc_ShortCut_GEN();
		var maxRuleApps = new MaximalRuleApplicationsTerminationCondition(allRules, -1);

		INodeSampler sampler = (String type, String ruleName, String nodeName) -> {
			switch (type) {
			case API_ExtTypeModel.ExtTypeModel__Project:
				return 1;
			default:
				return INodeSampler.EMPTY;
			}
		};

		return new NeoGenerator(//
				allRules, //
				new NoOpStartup(), //
				new CompositeTerminationConditionForGEN(1, TimeUnit.MINUTES, maxRuleApps), //
				new TwoPhaseRuleSchedulerForGEN(sampler), //
				new TwoPhaseUpdatePolicyForGEN(maxRuleApps), //
				new ParanoidNeoReprocessor(), //
				new NoOpCleanup(), //
				new HeartBeatAndReportMonitor(), //
				new ModelNameValueGenerator(SRC_MODEL_NAME, TRG_MODEL_NAME), //
				List.of(new LoremIpsumStringValueGenerator()));
	}
}
