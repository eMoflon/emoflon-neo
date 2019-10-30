package org.emoflon.neo.example.companytoit;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.API_CompanyToIT;
import org.emoflon.neo.api.CompanyToIT.API_CompanyToIT_GEN;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.matchreprocessors.ParanoidNeoReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.AllRulesAllMatchesScheduler;
import org.emoflon.neo.engine.modules.terminationcondition.CompositeTerminationConditionForGEN;
import org.emoflon.neo.engine.modules.terminationcondition.MaximalRuleApplicationsTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.RandomSingleMatchUpdatePolicy;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;

import run.CompanyToIT_CO_Run;
import run.CompanyToIT_GEN_Run;

public class GEN_CO_Tests extends ENeoTest {

	private void runTest(Consumer<MaximalRuleApplicationsTerminationCondition> configurator) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		var testCOApp = new CompanyToIT_CO_Run();
		var testGenApp = new CompanyToIT_GEN_TEST(configurator);
		testGenApp.runGenerator();
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
			scheduler.setMaxNoOfApplicationsFor(API_CompanyToIT.CompanyToIT_CompanyToITRule, 1);
		});
	}

	@Test
	public void testOneOfEach() throws Exception {
		runTest((scheduler) -> {
			scheduler.setMaxNoOfApplicationsFor(API_CompanyToIT.CompanyToIT_CompanyToITRule, 1);
			scheduler.setMaxNoOfApplicationsFor(API_CompanyToIT.CompanyToIT_AdminToRouterRule, 1);
			scheduler.setMaxNoOfApplicationsFor(API_CompanyToIT.CompanyToIT_EmployeeToLaptopRule, 1);
			scheduler.setMaxNoOfApplicationsFor(API_CompanyToIT.CompanyToIT_EmployeeToPCRule, 1);
		});
	}

	@Test
	public void test10OfEach() throws Exception {
		runTest((scheduler) -> {
			scheduler.setMaxNoOfApplicationsFor(API_CompanyToIT.CompanyToIT_CompanyToITRule, 10);
			scheduler.setMaxNoOfApplicationsFor(API_CompanyToIT.CompanyToIT_AdminToRouterRule, 10);
			scheduler.setMaxNoOfApplicationsFor(API_CompanyToIT.CompanyToIT_EmployeeToLaptopRule, 10);
			scheduler.setMaxNoOfApplicationsFor(API_CompanyToIT.CompanyToIT_EmployeeToPCRule, 10);
		});
	}

	@Test
	public void tryLotsOfAdmins() throws Exception {
		runTest((scheduler) -> {
			scheduler.setMaxNoOfApplicationsFor(API_CompanyToIT.CompanyToIT_CompanyToITRule, 1);
			scheduler.setMaxNoOfApplicationsFor(API_CompanyToIT.CompanyToIT_AdminToRouterRule, 100);
		});
	}
}

class CompanyToIT_GEN_TEST extends CompanyToIT_GEN_Run {
	private Consumer<MaximalRuleApplicationsTerminationCondition> configurator;

	public CompanyToIT_GEN_TEST(Consumer<MaximalRuleApplicationsTerminationCondition> configureScheduler) {
		this.configurator = configureScheduler;
	}

	@Override
	protected NeoGenerator createGenerator(API_CompanyToIT_GEN genAPI) {
		var allRules = genAPI.getAllRulesForCompanyToIT__GEN();
		var ruleScheduler = new MaximalRuleApplicationsTerminationCondition(allRules, 0);
		configurator.accept(ruleScheduler);

		return new NeoGenerator(//
				allRules, //
				new CompositeTerminationConditionForGEN(30, TimeUnit.SECONDS, ruleScheduler), //
				new AllRulesAllMatchesScheduler(), //
				new RandomSingleMatchUpdatePolicy(), //
				new ParanoidNeoReprocessor(), //
				new HeartBeatAndReportMonitor(), //
				List.of(new LoremIpsumStringValueGenerator()));
	}

}
