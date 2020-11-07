package org.emoflon.neo.example.javatodoc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static run.JavaToDoc_GEN_Run.SRC_MODEL_NAME;
import static run.JavaToDoc_GEN_Run.TRG_MODEL_NAME;

import java.util.function.Consumer;

import org.emoflon.neo.api.javatodoc.API_JavaToDoc;
import org.emoflon.neo.api.javatodoc.run.JavaToDoc_CC_Run;
import org.emoflon.neo.api.javatodoc.run.JavaToDoc_CO_Run;
import org.emoflon.neo.engine.modules.terminationcondition.MaximalRuleApplicationsTerminationCondition;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;

public class GEN_CO_CC_Tests extends ENeoTest {

	private void runTest(Consumer<MaximalRuleApplicationsTerminationCondition> configurator) throws Exception {
		var testCOApp = new JavaToDoc_CO_Run(SRC_MODEL_NAME, TRG_MODEL_NAME, solver);
		var testCCApp = new JavaToDoc_CC_Run(SRC_MODEL_NAME, TRG_MODEL_NAME, solver);
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
