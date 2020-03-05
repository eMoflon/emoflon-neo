package org.emoflon.neo.example.facebooktoinstagramfase;

import static Transformations.run.FacebookToInstagramFASE_GEN_Run.SRC_MODEL_NAME;
import static Transformations.run.FacebookToInstagramFASE_GEN_Run.TRG_MODEL_NAME;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.emoflon.neo.api.API_Transformations;
import org.emoflon.neo.api.Transformations.API_FacebookToInstagramFASE_GEN;
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

import Transformations.run.FacebookToInstagramFASE_CC_Run;
import Transformations.run.FacebookToInstagramFASE_CO_Run;
import Transformations.run.FacebookToInstagramFASE_GEN_Run;

public class GEN_CO_CC_Tests extends ENeoTest {

	private void runTest(Consumer<MaximalRuleApplicationsTerminationCondition> configurator) throws Exception {
		var testCOApp = new FacebookToInstagramFASE_CO_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testCCApp = new FacebookToInstagramFASE_CC_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testGenApp = new FacebookToInstagramFASE_GEN_TEST(configurator);

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
	public void testOnlyAxioms() throws Exception {
		runTest((scheduler) -> {
			scheduler.setMax(Transformations.run. 1)
					.setMax(API_Transformations.FacebookToInstagramFASE__UserToUserIslandRule, 1);
		});
	}

	@Test
	public void testOneOfEach() throws Exception {
		runTest((scheduler) -> {
			scheduler.setMax(API_Transformations.FacebookToInstagramFASE__NetworkToNetworkIslandRule, 1)
					.setMax(API_Transformations.FacebookToInstagramFASE__UserToUserIslandRule, 1)
					.setMax(API_Transformations.FacebookToInstagramFASE__UserNetworkBridgeRule, 1)
					.setMax(API_Transformations.FacebookToInstagramFASE__RequestFriendship, 1)
					.setMax(API_Transformations.FacebookToInstagramFASE__AcceptFriendship, 1)
					.setMax(API_Transformations.FacebookToInstagramFASE__IgnoreInterNetworkFollowers, 1)
					.setMax(API_Transformations.FacebookToInstagramFASE__ParentsIgnoreRule, 1)
					.setMax(API_Transformations.FacebookToInstagramFASE__SpouseIgnoreRule, 1)
					.setMax(API_Transformations.FacebookToInstagramFASE__SiblingsIgnoreRule, 1);
		});
	}

	@Test
	public void test10OfEach() throws Exception {
		runTest((scheduler) -> {
			scheduler.setMax(API_Transformations.FacebookToInstagramFASE__NetworkToNetworkIslandRule, 10)
					.setMax(API_Transformations.FacebookToInstagramFASE__UserToUserIslandRule, 10)
					.setMax(API_Transformations.FacebookToInstagramFASE__UserNetworkBridgeRule, 10)
					.setMax(API_Transformations.FacebookToInstagramFASE__RequestFriendship, 10)
					.setMax(API_Transformations.FacebookToInstagramFASE__AcceptFriendship, 10)
					.setMax(API_Transformations.FacebookToInstagramFASE__IgnoreInterNetworkFollowers, 10)
					.setMax(API_Transformations.FacebookToInstagramFASE__ParentsIgnoreRule, 10)
					.setMax(API_Transformations.FacebookToInstagramFASE__SpouseIgnoreRule, 10)
					.setMax(API_Transformations.FacebookToInstagramFASE__SiblingsIgnoreRule, 10);
		});
	}

	@Test
	public void tryLotsOfUsers() throws Exception {
		runTest((scheduler) -> {
			scheduler.setMax(API_Transformations.FacebookToInstagramFASE__NetworkToNetworkIslandRule, 1)
					.setMax(API_Transformations.FacebookToInstagramFASE__UserToUserIslandRule, 100)
					.setMax(API_Transformations.FacebookToInstagramFASE__UserNetworkBridgeRule, 100);
		});
	}
}

class FacebookToInstagramFASE_GEN_TEST extends FacebookToInstagramFASE_GEN_Run {
	private Consumer<MaximalRuleApplicationsTerminationCondition> configurator;

	public FacebookToInstagramFASE_GEN_TEST(Consumer<MaximalRuleApplicationsTerminationCondition> configureScheduler) {
		super(SRC_MODEL_NAME, TRG_MODEL_NAME);
		this.configurator = configureScheduler;
	}

	@Override
	public NeoGenerator createGenerator(NeoCoreBuilder builder) {
		var allRules = new API_FacebookToInstagramFASE_GEN(builder).getAllRulesForFacebookToInstagramFASE_GEN();
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
				new ModelNameValueGenerator(SRC_MODEL_NAME, TRG_MODEL_NAME), //
				List.of(new LoremIpsumStringValueGenerator()));
	}

}
