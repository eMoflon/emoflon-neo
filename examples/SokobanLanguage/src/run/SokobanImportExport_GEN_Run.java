package run;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.rules.API_SokobanTGGs;
import org.emoflon.neo.api.rules.SokobanTGGs.API_SokobanImportExport_GEN;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
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

/**
 * Generator is configured to generate a single board with a square formation.
 * 
 * @author anthonyanjorin
 */
public class SokobanImportExport_GEN_Run extends rules.SokobanTGGs.run.SokobanImportExport_GEN_Run {

	public static void main(String[] args) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		var app = new SokobanImportExport_GEN_Run();
		app.run();
	}

	@Override
	public NeoGenerator createGenerator(NeoCoreBuilder builder) {
		var allRules = new API_SokobanImportExport_GEN(builder).getAllRulesForSokobanImportExport_GEN();

		var maxRuleApps = new MaximalRuleApplicationsTerminationCondition(allRules, 0);
		maxRuleApps.setMax(API_SokobanTGGs.SokobanImportExport__BoardNormalEntryRule, 1);
		maxRuleApps.setMax(API_SokobanTGGs.SokobanImportExport__FirstRowAllColsEnd, 2);
		maxRuleApps.setMax(API_SokobanTGGs.SokobanImportExport__FirstRowAllColsNormal, 2);
		maxRuleApps.setMax(API_SokobanTGGs.SokobanImportExport__FirstColAllRowsNormal, 2);
		maxRuleApps.setMax(API_SokobanTGGs.SokobanImportExport__FirstColAllRowsEnd, 2);
		maxRuleApps.setMax(API_SokobanTGGs.SokobanImportExport__AllOtherFieldsEnd, -1);
		maxRuleApps.setMax(API_SokobanTGGs.SokobanImportExport__AllOtherFieldsNormal, -1);

		INodeSampler sampler = (String type, String ruleName, String nodeName) -> {
			return INodeSampler.EMPTY;
		};

		return new NeoGenerator(//
				allRules, //
				new NoOpStartup(), //
				new CompositeTerminationConditionForGEN(5, TimeUnit.MINUTES, maxRuleApps), //
				new TwoPhaseRuleSchedulerForGEN(sampler), //
				new TwoPhaseUpdatePolicyForGEN(maxRuleApps), //
				new ParanoidNeoReprocessor(), //
				new NoOpCleanup(), //
				new HeartBeatAndReportMonitor(), //
				new ModelNameValueGenerator(SRC_MODEL_NAME, TRG_MODEL_NAME), //
				List.of(new LoremIpsumStringValueGenerator()));
	}
}
