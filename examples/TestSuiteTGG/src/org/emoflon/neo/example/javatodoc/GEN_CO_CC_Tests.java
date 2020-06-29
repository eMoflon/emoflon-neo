package org.emoflon.neo.example.javatodoc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static run.JavaToDoc_GEN_Run.SRC_MODEL_NAME;
import static run.JavaToDoc_GEN_Run.TRG_MODEL_NAME;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.emoflon.neo.api.API_JavaToDoc;
import org.emoflon.neo.api.JavaToDoc.API_JavaToDoc_GEN;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.cleanup.NoOpCleanup;
import org.emoflon.neo.engine.modules.matchreprocessors.ParanoidNeoReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.AllRulesAllMatchesScheduler;
import org.emoflon.neo.engine.modules.startup.NoOpStartup;
import org.emoflon.neo.engine.modules.terminationcondition.CompositeTerminationConditionForGEN;
import org.emoflon.neo.engine.modules.terminationcondition.MaximalRuleApplicationsTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.RandomSingleMatchUpdatePolicy;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;

import JavaToDoc.run.JavaToDoc_CC_Run;
import JavaToDoc.run.JavaToDoc_CO_Run;
import JavaToDoc.run.JavaToDoc_GEN_Run;

public class GEN_CO_CC_Tests extends ENeoTest {

	private void runTest(Consumer<MaximalRuleApplicationsTerminationCondition> configurator) throws Exception {
		var testCOApp = new JavaToDoc_CO_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testCCApp = new JavaToDoc_CC_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testGenApp = new JavaToDoc_GEN_TEST(configurator);

		// Step 1. Run GEN to produce a triple
		testGenApp.run();

		// Step 2. Check that produced triple is consistent with CO
		assertTrue(testCOApp.runCheckOnly().isConsistent());

		// Step 3. Remove corrs to produce input for CC
		builder.deleteAllCorrs();

		// Step 4: Create corrs
		assertTrue(testCCApp.runCorrCreation().isConsistent());

		// Step 5: Check that consistency has been restored
		assertTrue(testCOApp.runCheckOnly().isConsistent());
	}

	@Test
	public void testEmptyTriple() throws Exception {
		runTest((scheduler) -> {
		});
	}

	@Test
	public void testOnlyAxiom() throws Exception {
		runTest((scheduler) -> {
			scheduler.setMax(API_JavaToDoc.JavaToDoc__RootToRootRule, 1);
		});
	}

	@Test
	public void testOneOfEach() throws Exception {
		runTest((scheduler) -> {
			scheduler.setMax(API_JavaToDoc.JavaToDoc__RootToRootRule, 1)
					.setMax(API_JavaToDoc.JavaToDoc__ClazzToDocRule, 1)
					.setMax(API_JavaToDoc.JavaToDoc__SubToSubRule, 1);
		});
	}

	@Test
	public void test10OfEach() throws Exception {
		runTest((scheduler) -> {
			scheduler.setMax(API_JavaToDoc.JavaToDoc__RootToRootRule, 10)
			.setMax(API_JavaToDoc.JavaToDoc__ClazzToDocRule, 10)
			.setMax(API_JavaToDoc.JavaToDoc__SubToSubRule, 10);
		});
	}

	@Test
	public void tryLotsOfClazzes() throws Exception {
		runTest((scheduler) -> {
			scheduler.setMax(API_JavaToDoc.JavaToDoc__RootToRootRule, 100)
			.setMax(API_JavaToDoc.JavaToDoc__ClazzToDocRule, 100);
		});
	}
}

class JavaToDoc_GEN_TEST extends JavaToDoc_GEN_Run {
	private Consumer<MaximalRuleApplicationsTerminationCondition> configurator;

	public JavaToDoc_GEN_TEST(Consumer<MaximalRuleApplicationsTerminationCondition> configureScheduler) {
		super(SRC_MODEL_NAME, TRG_MODEL_NAME);
		this.configurator = configureScheduler;
	}

	@Override
	public NeoGenerator createGenerator(NeoCoreBuilder builder) {
		var allRules = new API_JavaToDoc_GEN(builder).getAllRulesForJavaToDoc_GEN();
		var ruleScheduler = new MaximalRuleApplicationsTerminationCondition(allRules, 0);
		configurator.accept(ruleScheduler);

		return new NeoGenerator(//
				allRules, //
				new NoOpStartup(), //
				new CompositeTerminationConditionForGEN(1, TimeUnit.MINUTES, ruleScheduler), //
				new AllRulesAllMatchesScheduler(), //
				new RandomSingleMatchUpdatePolicy(), //
				new ParanoidNeoReprocessor(), //
				new NoOpCleanup(), //
				new HeartBeatAndReportMonitor(), //
				new ModelNameValueGenerator("Source", "Target"), //
				List.of(new LoremIpsumStringValueGenerator()));
	}

}
