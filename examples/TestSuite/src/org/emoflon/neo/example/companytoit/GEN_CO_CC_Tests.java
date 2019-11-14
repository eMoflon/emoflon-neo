package org.emoflon.neo.example.companytoit;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.emoflon.neo.api.API_CompanyToIT;
import org.emoflon.neo.api.CompanyToIT.API_CompanyToIT_GEN;
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

import run.CompanyToIT_CC_Run;
import run.CompanyToIT_CO_Run;
import run.CompanyToIT_GEN_Run;

public class GEN_CO_CC_Tests extends ENeoTest {

	private void runTest(Consumer<MaximalRuleApplicationsTerminationCondition> configurator) throws Exception {
		var testCOApp = new CompanyToIT_CO_Run();
		var testCCApp = new CompanyToIT_CC_Run();
		var testGenApp = new CompanyToIT_GEN_TEST(configurator);

		// Step 1. Run GEN to produce a triple
		testGenApp.runGenerator();

		// Step 2. Check that produced triple is consistent with CO
		assertTrue(testCOApp.runCheckOnly());

		// Step 3. Remove corrs to produce input for CC
		builder.deleteAllCorrs();

		// Step 4: Create corrs
		assertTrue(testCCApp.runCorrCreation());

		// Step 5: Check that consistency has been restored
		assertTrue(testCOApp.runCheckOnly());
	}

	@Test
	public void testEmptyTriple() throws Exception {
		runTest((scheduler) -> {
		});
	}

	@Test
	public void testOnlyAxiom() throws Exception {
		runTest((scheduler) -> {
			scheduler.setMax(API_CompanyToIT.CompanyToIT__CompanyToITRule, 1);
		});
	}

	@Test
	public void testOneOfEach() throws Exception {
		runTest((scheduler) -> {
			scheduler.setMax(API_CompanyToIT.CompanyToIT__CompanyToITRule, 1)
					.setMax(API_CompanyToIT.CompanyToIT__AdminToRouterRule, 1)
					.setMax(API_CompanyToIT.CompanyToIT__EmployeeToLaptopRule, 1)
					.setMax(API_CompanyToIT.CompanyToIT__EmployeeToPCRule, 1);
		});
	}

	@Test
	public void test10OfEach() throws Exception {
		runTest((scheduler) -> {
			scheduler.setMax(API_CompanyToIT.CompanyToIT__CompanyToITRule, 10)
					.setMax(API_CompanyToIT.CompanyToIT__AdminToRouterRule, 10)
					.setMax(API_CompanyToIT.CompanyToIT__EmployeeToLaptopRule, 10)
					.setMax(API_CompanyToIT.CompanyToIT__EmployeeToPCRule, 10);
		});
	}

	@Test
	public void tryLotsOfAdmins() throws Exception {
		runTest((scheduler) -> {
			scheduler.setMax(API_CompanyToIT.CompanyToIT__CompanyToITRule, 100)
					.setMax(API_CompanyToIT.CompanyToIT__AdminToRouterRule, 100);
		});
	}
}

class CompanyToIT_GEN_TEST extends CompanyToIT_GEN_Run {
	private Consumer<MaximalRuleApplicationsTerminationCondition> configurator;

	public CompanyToIT_GEN_TEST(Consumer<MaximalRuleApplicationsTerminationCondition> configureScheduler) {
		this.configurator = configureScheduler;
	}

	@Override
	protected NeoGenerator createGenerator(NeoCoreBuilder builder) {
		var allRules = new API_CompanyToIT_GEN(builder).getAllRulesForCompanyToIT__GEN();
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
