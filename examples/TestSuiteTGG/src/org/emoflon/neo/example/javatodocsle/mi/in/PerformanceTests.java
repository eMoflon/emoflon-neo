package org.emoflon.neo.example.javatodocsle.mi.in;


import static org.emoflon.neo.api.javatodocsle.run.JavaToDocSLE_GEN_Run.SRC_MODEL_NAME;
import static org.emoflon.neo.api.javatodocsle.run.JavaToDocSLE_GEN_Run.TRG_MODEL_NAME;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.javatodocsle.API_JavaToDocSLE;
import org.emoflon.neo.api.javatodocsle.run.JavaToDocSLE_MI_Run;
import org.emoflon.neo.api.testsuitetgg.org.emoflon.neo.example.javatodocsle.mi.in.API_ConflictGenerator;
import org.emoflon.neo.engine.modules.terminationcondition.MaximalRuleApplicationsTerminationCondition;
import org.emoflon.neo.example.ENeoTest;
import org.junit.Ignore;

import org.emoflon.neo.api.javatodocsle.run.JavaToDocSLE_BWD_OPT_Run;
import org.emoflon.neo.api.javatodocsle.run.JavaToDocSLE_CC_Run;
import org.emoflon.neo.api.javatodocsle.run.JavaToDocSLE_CO_Run;
import org.emoflon.neo.api.javatodocsle.run.JavaToDocSLE_FWD_OPT_Run;

import org.junit.jupiter.api.Test;;

public class PerformanceTests extends ENeoTest {
	
	public void runTestForFixedSize(int nrOfApplications, int nrOfIterations) throws Exception {
		
		Logger.getRootLogger().setLevel(Level.INFO);
		var testCOApp = new JavaToDocSLE_CO_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testCCApp = new JavaToDocSLE_CC_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testFWD_OPTApp = new JavaToDocSLE_FWD_OPT_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testBWD_OPTApp = new JavaToDocSLE_BWD_OPT_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		
		var testGenApp = new JavaToDocSLE_GEN_TEST((scheduler) -> {
			scheduler.setMax(API_JavaToDocSLE.JavaToDocSLE__ClazzToDocRule, 10 * nrOfApplications)
			.setMax(API_JavaToDocSLE.JavaToDocSLE__SubClazzToSubDocRule, 10 * nrOfApplications)
			.setMax(API_JavaToDocSLE.JavaToDocSLE__MethodToEntryRule, 40 * nrOfApplications)
			.setMax(API_JavaToDocSLE.JavaToDocSLE__FieldToEntryRule, 40 * nrOfApplications)
			.setMax(API_JavaToDocSLE.JavaToDocSLE__AddParameterRule, 40 * nrOfApplications);
//			.setMax(API_JavaToDocSLE.JavaToDocSLE__AddGlossaryRule, 1)
//			.setMax(API_JavaToDocSLE.JavaToDocSLE__AddGlossaryEntryRule, 40 * nrOfApplications)
//			.setMax(API_JavaToDocSLE.JavaToDocSLE__LinkGlossaryEntryRule, 40 * nrOfApplications);
		});
		
		// Step 2. Run ops
//		for (int j=0; j<nrOfIterations; j++) {
//			testGenApp.run();
//			Logger.getRootLogger().setLevel(Level.INFO);
//			logger.info("CO, Iteration: " + j + ". Start new configuration: " + nrOfApplications * 1000 + " elements...");
//			testCOApp.run();
//			Logger.getRootLogger().setLevel(Level.OFF);
//			clearDB();
//		}
//		
//		for (int j=0; j<nrOfIterations; j++) {
//			testGenApp.run();
//			builder.deleteAllCorrs();
//			Logger.getRootLogger().setLevel(Level.INFO);
//			logger.info("CC, Iteration: " + j + ". Start new configuration: " + nrOfApplications * 1000 + " elements...");
//			testCCApp.run();
//			Logger.getRootLogger().setLevel(Level.OFF);
//			clearDB();
//		}
		for (int j=0; j<nrOfIterations; j++) {
			testGenApp.run();
			builder.deleteAllCorrs();
			builder.clearModel(TRG_MODEL_NAME);
			Logger.getRootLogger().setLevel(Level.INFO);
			logger.info("FWD_OPT, Iteration: " + j + ". Start new configuration: " + nrOfApplications * 1000 + " elements...");
			testFWD_OPTApp.run();
			Logger.getRootLogger().setLevel(Level.OFF);
			clearDB();
		}
		
//		for (int j=0; j<nrOfIterations; j++) {
//			testGenApp.run();
//			builder.deleteAllCorrs();
//			builder.clearModel(SRC_MODEL_NAME);
//			Logger.getRootLogger().setLevel(Level.INFO);
//			logger.info("BWD_OPT, Iteration: " + j + ". Start new configuration: " + nrOfApplications * 1000 + " elements...");
//			testBWD_OPTApp.run();
//			Logger.getRootLogger().setLevel(Level.OFF);
//			clearDB();
//		}
	}
	
	@Ignore("Performance tests are not part of the test suite, enable it for testing performance")
	public void testPerformance() throws Exception {
		for (int i : new int[]{1,2,5,10,20,50}) {
			Logger.getRootLogger().setLevel(Level.OFF);
			runTestForFixedSize(i, 5);
		}
	}
}