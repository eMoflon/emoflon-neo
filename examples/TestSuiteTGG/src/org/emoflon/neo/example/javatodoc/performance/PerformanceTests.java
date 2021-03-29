package org.emoflon.neo.example.javatodoc.performance;

import static org.emoflon.neo.api.javatodocsle.run.JavaToDocSLE_GEN_Run.SRC_MODEL_NAME;
import static org.emoflon.neo.api.javatodocsle.run.JavaToDocSLE_GEN_Run.TRG_MODEL_NAME;
import org.emoflon.neo.api.testsuitetgg.org.emoflon.neo.example.javatodoc.performance.API_ConflictGenerator;
import org.emoflon.neo.api.testsuitetgg.org.emoflon.neo.example.javatodoc.performance.in.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.javatodocsle.API_JavaToDocSLE;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
import org.emoflon.neo.example.ENeoTest;
import org.junit.Ignore;

import org.emoflon.neo.api.javatodocsle.run.JavaToDocSLE_BWD_OPT_Run;
import org.emoflon.neo.api.javatodocsle.run.JavaToDocSLE_CC_Run;
import org.emoflon.neo.api.javatodocsle.run.JavaToDocSLE_CO_Run;
import org.emoflon.neo.api.javatodocsle.run.JavaToDocSLE_FWD_OPT_Run;

public class PerformanceTests extends ENeoTest {
	
	private API_ConflictGenerator api = new API_ConflictGenerator(builder);
	private API_JavaModel0 api_j0 = new API_JavaModel0(builder);
	private API_CreateCorrs0 api_c0 = new API_CreateCorrs0(builder);
	private API_DocModel0 api_d0 = new API_DocModel0(builder);
	private API_JavaModel1 api_j1 = new API_JavaModel1(builder);
	private API_CreateCorrs1 api_c1 = new API_CreateCorrs1(builder);
	private API_DocModel1 api_d1 = new API_DocModel1(builder);
	private API_JavaModel2 api_j2 = new API_JavaModel2(builder);
	private API_CreateCorrs2 api_c2 = new API_CreateCorrs2(builder);
	private API_DocModel2 api_d2 = new API_DocModel2(builder);
	private API_JavaModel3 api_j3 = new API_JavaModel3(builder);
	private API_CreateCorrs3 api_c3 = new API_CreateCorrs3(builder);
	private API_DocModel3 api_d3 = new API_DocModel3(builder);
	private API_JavaModel4 api_j4 = new API_JavaModel4(builder);
	private API_CreateCorrs4 api_c4 = new API_CreateCorrs4(builder);
	private API_DocModel4 api_d4 = new API_DocModel4(builder);
	
	private static final int nrOfIterations = 30;
	private static final SupportedILPSolver solver = SupportedILPSolver.Gurobi;
	
	public void runTestForRandomModels(int nrOfApplications, int nrOfConflicts) throws Exception {
		
		var testCOApp = new JavaToDocSLE_CO_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testCCApp = new JavaToDocSLE_CC_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testFWD_OPTApp = new JavaToDocSLE_FWD_OPT_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testBWD_OPTApp = new JavaToDocSLE_BWD_OPT_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testMIApp = new JavaToDocSLE_MI_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		
		var testGenApp = new JavaToDocSLE_GEN_TEST((scheduler) -> {
			scheduler.setMax(API_JavaToDocSLE.JavaToDocSLE__ClazzToDocRule, 10 * nrOfApplications)
			.setMax(API_JavaToDocSLE.JavaToDocSLE__SubClazzToSubDocRule, 10 * nrOfApplications)
			.setMax(API_JavaToDocSLE.JavaToDocSLE__MethodToEntryRule, 40 * nrOfApplications)
			.setMax(API_JavaToDocSLE.JavaToDocSLE__FieldToEntryRule, 40 * nrOfApplications)
			.setMax(API_JavaToDocSLE.JavaToDocSLE__AddParameterRule, 40 * nrOfApplications)
			.setMax(API_JavaToDocSLE.JavaToDocSLE__AddGlossaryRule, 1)
			.setMax(API_JavaToDocSLE.JavaToDocSLE__AddGlossaryEntryRule, 40 * nrOfApplications)
			.setMax(API_JavaToDocSLE.JavaToDocSLE__LinkGlossaryEntryRule, 40 * nrOfApplications);
		});
		
		// Step 2. Run ops
		for (int j=0; j<nrOfIterations; j++) {
			testGenApp.run();
			Logger.getRootLogger().setLevel(Level.INFO);
			logger.info("CO, Iteration: " + j + ". Start new configuration: " + nrOfApplications * 1000 + " elements...");
			testCOApp.run();
			Logger.getRootLogger().setLevel(Level.OFF);
			clearDB();
		}
		
		for (int j=0; j<nrOfIterations; j++) {
			testGenApp.run();
			builder.deleteAllCorrs();
			Logger.getRootLogger().setLevel(Level.INFO);
			logger.info("CC, Iteration: " + j + ". Start new configuration: " + nrOfApplications * 1000 + " elements...");
			testCCApp.run();
			Logger.getRootLogger().setLevel(Level.OFF);
			clearDB();
		}
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
		for (int j=0; j<nrOfIterations; j++) {
			testGenApp.run();
			builder.deleteAllCorrs();
			builder.clearModel(SRC_MODEL_NAME);
			Logger.getRootLogger().setLevel(Level.INFO);
			logger.info("BWD_OPT, Iteration: " + j + ". Start new configuration: " + nrOfApplications * 1000 + " elements...");
			testBWD_OPTApp.run();
			Logger.getRootLogger().setLevel(Level.OFF);
			clearDB();
		}
		
		for (int j=0; j<nrOfIterations; j++) {
			testGenApp.run();
			for (int i=0; i < nrOfConflicts; i++) {
				api.getRule_CreateDeleteConflict().rule().apply();
				api.getRule_MoveMoveConflict().rule().apply();
				api.getRule_MoveDeleteConflict().rule().apply();
			}
			Logger.getRootLogger().setLevel(Level.INFO);
			logger.info("MI, Iteration: " + j + ". Start new configuration: " + nrOfApplications * 1000 + " elements...");
			testMIApp.run(solver);
			Logger.getRootLogger().setLevel(Level.OFF);
			clearDB();
		}
	}
	
	@Ignore("Performance tests are not part of the test suite, enable it for testing performance")
	public void testPerformanceForRandomModels() throws Exception {
		for (int i : new int[]{1,2,5,10,20,50}) {
			Logger.getRootLogger().setLevel(Level.OFF);
			runTestForRandomModels(i, i);
		}
	}
	
	@Ignore("Performance Tests are not part of the test suite.")
	public void testPerformanceForFixedModels() throws Exception {
		for (int modelID=0; modelID<5; modelID++) {
			logger.info("###################################");
			logger.info("### Performance Test:  " + (modelID+1) + " Models ###");
			logger.info("###################################");
			Logger.getRootLogger().setLevel(Level.INFO);
			
			for (int i=0; i<nrOfIterations; i++) {
				logger.info("### Iteration " + i);
				runTestForFixedModels(modelID, solver);
			}
		}
	}
	
	public void runTestForFixedModels(int n, SupportedILPSolver s) throws Exception {
			
		// Export models
		switch (n) {
		case 4:
			exportTriple(api_j4.getModel_MoflonJava4(), //
					api_d4.getModel_MoflonDoc4());
			break;
		case 3: 
			exportTriple(api_j3.getModel_MoflonJava3(), //
					api_d3.getModel_MoflonDoc3());
			break;
		case 2:
			exportTriple(api_j2.getModel_MoflonJava2(), //
					api_d2.getModel_MoflonDoc2());
			break;
		case 1:
			exportTriple(api_j1.getModel_MoflonJava1(), //
					api_d1.getModel_MoflonDoc1());
			break;
		case 0:
			exportTriple(api_j0.getModel_MoflonJava0(), //
					api_d0.getModel_MoflonDoc0());
			break;
		}
		
		// Export rules
		switch (n) {
		case 4:
			api_c4.getRule_CreateClazzToDoc4().rule().apply();
			api_c4.getRule_CreateMethodToEntry4().rule().apply();
			api_c4.getRule_CreateFieldToEntry4().rule().apply();
		case 3: 
			api_c3.getRule_CreateClazzToDoc3().rule().apply();
			api_c3.getRule_CreateMethodToEntry3().rule().apply();
			api_c3.getRule_CreateFieldToEntry3().rule().apply();
		case 2:
			api_c2.getRule_CreateClazzToDoc2().rule().apply();
			api_c2.getRule_CreateMethodToEntry2().rule().apply();
			api_c2.getRule_CreateFieldToEntry2().rule().apply();
		case 1:
			api_c1.getRule_CreateClazzToDoc1().rule().apply();
			api_c1.getRule_CreateMethodToEntry1().rule().apply();
			api_c1.getRule_CreateFieldToEntry1().rule().apply();
		case 0:
			api_c0.getRule_CreateClazzToDoc0().rule().apply();
			api_c0.getRule_CreateMethodToEntry0().rule().apply();
			api_c0.getRule_CreateFieldToEntry0().rule().apply();
		}

		
		new JavaToDocSLE_MI_Run("moflonJava" + n, "moflonDoc" + n).run(solver);
		clearDB();
	}
}