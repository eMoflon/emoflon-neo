package org.emoflon.neo.example.javatodoc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static run.JavaToDoc_GEN_Run.SRC_MODEL_NAME;
import static run.JavaToDoc_GEN_Run.TRG_MODEL_NAME;

import java.util.function.Consumer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.javatodoc.API_JavaToDoc;
import org.emoflon.neo.api.javatodoc.run.JavaToDoc_CO_Run;
import org.emoflon.neo.api.javatodoc.run.JavaToDoc_MI_Run;
import org.emoflon.neo.api.testsuitetgg.org.emoflon.neo.example.javatodoc.mi.in.API_ConflictGenerator;
import org.emoflon.neo.engine.modules.terminationcondition.MaximalRuleApplicationsTerminationCondition;
import org.emoflon.neo.example.ENeoTest;
import org.junit.Ignore;

public class PerformanceTests extends ENeoTest {

	private void runTest(Consumer<MaximalRuleApplicationsTerminationCondition> configurator, int nrOfConflictsEach) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		API_ConflictGenerator api = new API_ConflictGenerator(builder);
		
		var testCOApp = new JavaToDoc_CO_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testGenApp = new JavaToDoc_GEN_TEST(configurator);
		var testMIApp = new JavaToDoc_MI_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var createDeleteConflict = api.getRule_CreateDeleteConflict().rule();
		var moveDeleteConflict = api.getRule_MoveDeleteConflict().rule();
		var subSubOppositeConflict = api.getRule_SubSubOppositeConflict().rule();
		
		// Step 1. Run GEN to produce a triple
		testGenApp.run();

		// Step 2. Create conflicts by modifying the triple 
		for (int i=0; i< nrOfConflictsEach; i++) {
			assertTrue(createDeleteConflict.apply().isPresent());
			assertTrue(moveDeleteConflict.apply().isPresent());
			assertTrue(subSubOppositeConflict.apply().isPresent());
		}
		
		// Step 3. Run model integration
		testMIApp.run();
		
		// Step 4. Check that produced triple is consistent with CO
		assertTrue(testCOApp.runCheckOnly().isConsistent());

	}

	@Ignore
	public void test10OfEach() throws Exception {
		runTest((scheduler) -> {
			scheduler.setMax(API_JavaToDoc.JavaToDoc__RootToRootRule, 100)
			.setMax(API_JavaToDoc.JavaToDoc__ClazzToDocRule, 300)
			.setMax(API_JavaToDoc.JavaToDoc__SubToSubRule, 300);
		}, 30);
	}
}