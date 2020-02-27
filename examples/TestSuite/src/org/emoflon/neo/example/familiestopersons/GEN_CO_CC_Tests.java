package org.emoflon.neo.example.familiestopersons;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static run.FamiliesToPersons_GEN_Run.TRG_MODEL_NAME;
import static run.FamiliesToPersons_GEN_Run.SRC_MODEL_NAME;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.emoflon.neo.api.API_DaughterOfExistingFamilyToFemale;
import org.emoflon.neo.api.API_DaughterToFemale;
import org.emoflon.neo.api.API_Families2Persons;
import org.emoflon.neo.api.API_FatherOfExistingFamilyToMale;
import org.emoflon.neo.api.API_FatherToMale;
import org.emoflon.neo.api.API_MotherOfExistingFamilyToFemale;
import org.emoflon.neo.api.API_MotherToFemale;
import org.emoflon.neo.api.API_SonOfExistingFamilyToMale;
import org.emoflon.neo.api.API_SonToMale;
import org.emoflon.neo.api.Schema.API_FamiliesToPersons_GEN;
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

import Schema.run.FamiliesToPersons_CC_Run;
import Schema.run.FamiliesToPersons_CO_Run;
import Schema.run.FamiliesToPersons_GEN_Run;

public class GEN_CO_CC_Tests extends ENeoTest {

	private void runTest(Consumer<MaximalRuleApplicationsTerminationCondition> configurator) throws Exception {
		var testCOApp = new FamiliesToPersons_CO_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testCCApp = new FamiliesToPersons_CC_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testGenApp = new FamiliesToPersons_GEN_TEST(configurator);

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
			scheduler.setMax(API_Families2Persons.FamiliesToPersons__Families2Persons, 1);
		});
	}

	@Test
	public void testOneOfEach() throws Exception {
		runTest((scheduler) -> {
			scheduler
					.setMax(API_DaughterOfExistingFamilyToFemale.FamiliesToPersons__DaughterOfExistingFamilyToFemale, 1)
					.setMax(API_DaughterToFemale.FamiliesToPersons__DaughterToFemale, 1)
					.setMax(API_Families2Persons.FamiliesToPersons__Families2Persons, 1)
					.setMax(API_FatherOfExistingFamilyToMale.FamiliesToPersons__FatherOfExistingFamilyToMale, 1)
					.setMax(API_FatherToMale.FamiliesToPersons__FatherToMale, 1)
					.setMax(API_MotherOfExistingFamilyToFemale.FamiliesToPersons__MotherOfExistingFamilyToFemale, 1)
					.setMax(API_MotherToFemale.FamiliesToPersons__MotherToFemale, 1)
					.setMax(API_SonOfExistingFamilyToMale.FamiliesToPersons__SonOfExistingFamilyToMale, 1)
					.setMax(API_SonToMale.FamiliesToPersons__SonToMale, 1);
		});
	}

	@Test
	public void test10OfEach() throws Exception {
		runTest((scheduler) -> {
			scheduler
					.setMax(API_DaughterOfExistingFamilyToFemale.FamiliesToPersons__DaughterOfExistingFamilyToFemale,
							10)
					.setMax(API_DaughterToFemale.FamiliesToPersons__DaughterToFemale, 10)
					.setMax(API_Families2Persons.FamiliesToPersons__Families2Persons, 10)
					.setMax(API_FatherOfExistingFamilyToMale.FamiliesToPersons__FatherOfExistingFamilyToMale, 10)
					.setMax(API_FatherToMale.FamiliesToPersons__FatherToMale, 10)
					.setMax(API_MotherOfExistingFamilyToFemale.FamiliesToPersons__MotherOfExistingFamilyToFemale, 10)
					.setMax(API_MotherToFemale.FamiliesToPersons__MotherToFemale, 10)
					.setMax(API_SonOfExistingFamilyToMale.FamiliesToPersons__SonOfExistingFamilyToMale, 10)
					.setMax(API_SonToMale.FamiliesToPersons__SonToMale, 10);
		});
	}

}

class FamiliesToPersons_GEN_TEST extends FamiliesToPersons_GEN_Run {
	private Consumer<MaximalRuleApplicationsTerminationCondition> configurator;

	public FamiliesToPersons_GEN_TEST(Consumer<MaximalRuleApplicationsTerminationCondition> configureScheduler) {
		super(SRC_MODEL_NAME, TRG_MODEL_NAME);
		this.configurator = configureScheduler;
	}

	@Override
	public NeoGenerator createGenerator(NeoCoreBuilder builder) {
		var allRules = new API_FamiliesToPersons_GEN(builder).getAllRulesForFamiliesToPersons_GEN();
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
