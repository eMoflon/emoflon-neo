package org.emoflon.neo.example.javatodocsle.mi.in;

import static run.JavaToDocSLE_GEN_Run.SRC_MODEL_NAME;
import static run.JavaToDocSLE_GEN_Run.TRG_MODEL_NAME;

import java.util.function.Consumer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.javatodocsle.API_JavaToDocSLE;
import org.emoflon.neo.api.testsuitetgg.org.emoflon.neo.example.javatodocsle.mi.in.API_ConflictGenerator;
import org.emoflon.neo.engine.modules.terminationcondition.MaximalRuleApplicationsTerminationCondition;
import org.emoflon.neo.example.ENeoTest;
import org.junit.Ignore;

import run.JavaToDocSLE_MI_Run;

public class PerformanceTests extends ENeoTest {

	private void runTest(Consumer<MaximalRuleApplicationsTerminationCondition> configurator, int nrOfConflictsEach) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		API_ConflictGenerator api = new API_ConflictGenerator(builder);
		
		var testGenApp = new JavaToDocSLE_GEN_TEST(configurator);
		var testMIApp = new JavaToDocSLE_MI_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var createDeleteConflict = api.getRule_CreateDeleteConflict().rule();
		var moveDeleteConflict = api.getRule_MoveDeleteConflict().rule();
		//var moveMoveConflict = api.getRule_MoveMoveConflict().rule();
		
		// Step 1. Run GEN to produce a triple
		testGenApp.run();

		// Step 2. Create conflicts by modifying the triple 
		for (int i=0; i< nrOfConflictsEach; i++) {
			createDeleteConflict.apply();
			moveDeleteConflict.apply();
			//moveMoveConflict.apply();
		}
		
		// Step 3. Run model integration
		testMIApp.run();

	}
	
	public void runTestForFixedSize(int nrOfApplications, int nrOfConflicts) throws Exception {
		runTest((scheduler) -> {
			scheduler.setMax(API_JavaToDocSLE.JavaToDocSLE__ClazzToDocRule, 10 * nrOfApplications)
			.setMax(API_JavaToDocSLE.JavaToDocSLE__SubClazzToSubDocRule, 10 * nrOfApplications)
			.setMax(API_JavaToDocSLE.JavaToDocSLE__MethodToEntryRule, 40 * nrOfApplications)
			.setMax(API_JavaToDocSLE.JavaToDocSLE__FieldToEntryRule, 40 * nrOfApplications)
			.setMax(API_JavaToDocSLE.JavaToDocSLE__AddParameterRule, 40 * nrOfApplications)
			.setMax(API_JavaToDocSLE.JavaToDocSLE__AddGlossaryRule, 1)
			.setMax(API_JavaToDocSLE.JavaToDocSLE__AddGlossaryEntryRule, 40 * nrOfApplications)
			.setMax(API_JavaToDocSLE.JavaToDocSLE__LinkGlossaryEntryRule, 40 * nrOfApplications);
		}, nrOfConflicts/2);
	}
	
	@Ignore
	public void testPerformance() throws Exception {
		for (int i=11; i<=12; i++) {
			for (int j=0; j<7; j++) {
				logger.info("Iteration: " + j + ". Start new configuration: " + i * 1000 + " elements, 100 conflicts...");
				runTestForFixedSize(i, 100);
				clearDB();
			}
		}
		for (int i=11; i<=12;i++) {
			for (int j=0; j<7; j++) {
				logger.info("Iteration: " + j + ". Start new configuration: 5000 elements, " + i * 20 + " conflicts...");
				runTestForFixedSize(5, i * 20);
				clearDB();
			}
		}
	}
}