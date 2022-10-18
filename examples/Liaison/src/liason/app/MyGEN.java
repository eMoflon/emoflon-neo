package liason.app;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.liaison.API_Common;
import org.emoflon.neo.api.liaison.run.RequirementsCoverage_GEN_Run;
import org.emoflon.neo.api.liaison.tgg.API_RequirementsCoverage_GEN;
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
import org.emoflon.neo.engine.modules.valueGenerators.RandomIntegerGenerator;

public class MyGEN extends RequirementsCoverage_GEN_Run {

	public static void main(String[] args) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		var app = new MyGEN(SRC_MODEL_NAME, TRG_MODEL_NAME);
		
		API_Common.createBuilder().clearDataBase();
		
		app.run();
	}
	
	public MyGEN(String srcModelName, String trgModelName) {
		super(srcModelName, trgModelName);
	}
	
	public NeoGenerator createGenerator(NeoCoreBuilder builder) {
		var allRules = new API_RequirementsCoverage_GEN(builder).getAllRulesForRequirementsCoverage_GEN();
		
		// Restrict applications per rule
		var maxRuleApps = new MaximalRuleApplicationsTerminationCondition(allRules, 2);
		
		INodeSampler sampler = (String type, String ruleName, String nodeName) -> {
			return INodeSampler.EMPTY;
		};
		
		// Add RandomIntegerGenerator
		return new NeoGenerator(//
				allRules, //
				new NoOpStartup(), //
				new CompositeTerminationConditionForGEN(2, TimeUnit.MINUTES, maxRuleApps), //
				new TwoPhaseRuleSchedulerForGEN(sampler), //
				new TwoPhaseUpdatePolicyForGEN(maxRuleApps), //
				new ParanoidNeoReprocessor(), //
				new NoOpCleanup(), //
				new HeartBeatAndReportMonitor(), //
				new ModelNameValueGenerator(srcModelName, trgModelName), //
				
				// Add generator for random ints
				List.of(new LoremIpsumStringValueGenerator(), new RandomIntegerGenerator()));
	}

}
