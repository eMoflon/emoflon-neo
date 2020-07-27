package org.emoflon.neo.example.javatodocsle.mi.in;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.emoflon.neo.api.JavaToDocSLE.API_JavaToDocSLE_GEN;
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
import org.emoflon.neo.engine.modules.updatepolicies.AllMatchesUpdatePolicy;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.RandomUUIDGenerator;

import JavaToDocSLE.run.JavaToDocSLE_GEN_Run;

class JavaToDocSLE_GEN_TEST extends JavaToDocSLE_GEN_Run {
	private Consumer<MaximalRuleApplicationsTerminationCondition> configurator;

	public JavaToDocSLE_GEN_TEST(Consumer<MaximalRuleApplicationsTerminationCondition> configureScheduler) {
		super(SRC_MODEL_NAME, TRG_MODEL_NAME);
		this.configurator = configureScheduler;
	}

	@Override
	public NeoGenerator createGenerator(NeoCoreBuilder builder) {
		var allRules = new API_JavaToDocSLE_GEN(builder).getAllRulesForJavaToDocSLE_GEN();
		var ruleScheduler = new MaximalRuleApplicationsTerminationCondition(allRules, 0);
		configurator.accept(ruleScheduler);
		
		INodeSampler sampler = (String type, String ruleName, String nodeName) -> {
			return INodeSampler.EMPTY;
		};
		
		return new NeoGenerator(//
				allRules, //
				new NoOpStartup(), //
				new CompositeTerminationConditionForGEN(10, TimeUnit.MINUTES, ruleScheduler), //
				new TwoPhaseRuleSchedulerForGEN(sampler), //
				new AllMatchesUpdatePolicy(), //
				new ParanoidNeoReprocessor(), //
				new NoOpCleanup(), //
				new HeartBeatAndReportMonitor(), //
				new ModelNameValueGenerator(srcModelName, trgModelName), //
				List.of(new RandomUUIDGenerator()));
	}

}