package org.emoflon.neo.example.classinhhier2db.performance;

import static org.emoflon.neo.api.classinhhier2db.run.ClassInhHier2DB_GEN_Run.SRC_MODEL_NAME;
import static org.emoflon.neo.api.classinhhier2db.run.ClassInhHier2DB_GEN_Run.TRG_MODEL_NAME;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.classinhhier2db.API_ClassInhHier2DB;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
import org.emoflon.neo.example.ENeoTest;
import org.junit.Ignore;
import org.emoflon.neo.api.classinhhier2db.run.ClassInhHier2DB_CC_Run;
import org.emoflon.neo.api.classinhhier2db.run.ClassInhHier2DB_CO_Run;
import org.emoflon.neo.api.classinhhier2db.run.ClassInhHier2DB_FWD_OPT_Run;

public class PerformanceTests extends ENeoTest {
	
	private static final int nrOfIterations = 5;
	private static final SupportedILPSolver solver = SupportedILPSolver.Gurobi;
	
	public void runTestForRandomModelsCO(int nrOfApplications) throws Exception {
		var testCOApp = new ClassInhHier2DB_CO_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testGenApp = new ClassInhHier2DB_GEN_TEST((scheduler) -> {
			scheduler.setMax(API_ClassInhHier2DB.ClassInhHier2DB__PackageToDatabaseRule, 10 * nrOfApplications)
			.setMax(API_ClassInhHier2DB.ClassInhHier2DB__ClassToTableRule, 30 * nrOfApplications)
			.setMax(API_ClassInhHier2DB.ClassInhHier2DB__SubClassToTableRule, 30 * nrOfApplications)
			.setMax(API_ClassInhHier2DB.ClassInhHier2DB__AttributeToColumnRule, 80 * nrOfApplications);
		});
		
		for (int j=0; j<nrOfIterations; j++) {
			try {
				testGenApp.run();
				Logger.getRootLogger().setLevel(Level.INFO);
				logger.info("CO, Iteration: " + j + ". Start new configuration: " + nrOfApplications * 1000 + " elements...");
				testCOApp.run();
				Logger.getRootLogger().setLevel(Level.OFF);
				clearDB();
			}
			catch (Exception e) {
				logger.info("CO, Iteration: " + j + ", configuration: " + nrOfApplications * 1000 + " elements failed.");
			}
		}
	}
	
	public void runTestForRandomModelsCC(int nrOfApplications) throws Exception {
		var testCCApp = new ClassInhHier2DB_CC_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testGenApp = new ClassInhHier2DB_GEN_TEST((scheduler) -> {
			scheduler.setMax(API_ClassInhHier2DB.ClassInhHier2DB__PackageToDatabaseRule, 10 * nrOfApplications)
			.setMax(API_ClassInhHier2DB.ClassInhHier2DB__ClassToTableRule, 30 * nrOfApplications)
			.setMax(API_ClassInhHier2DB.ClassInhHier2DB__SubClassToTableRule, 30 * nrOfApplications)
			.setMax(API_ClassInhHier2DB.ClassInhHier2DB__AttributeToColumnRule, 80 * nrOfApplications);
		});
		
		for (int j=0; j<nrOfIterations; j++) {
			try {
				testGenApp.run();
				builder.deleteAllCorrs();
				Logger.getRootLogger().setLevel(Level.INFO);
				logger.info("CC, Iteration: " + j + ". Start new configuration: " + nrOfApplications * 1000 + " elements...");
				testCCApp.run();
				Logger.getRootLogger().setLevel(Level.OFF);
				clearDB();
			}
			catch (Exception e) {
				logger.info("CC, Iteration: " + j + ", configuration: " + nrOfApplications * 1000 + " elements failed.");
			}
		}
	}
	
	public void runTestForRandomModelsFWD_OPT(int nrOfApplications) throws Exception {
		var testFWD_OPTApp = new ClassInhHier2DB_FWD_OPT_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testGenApp = new ClassInhHier2DB_GEN_TEST((scheduler) -> {
			scheduler.setMax(API_ClassInhHier2DB.ClassInhHier2DB__PackageToDatabaseRule, 10 * nrOfApplications)
			.setMax(API_ClassInhHier2DB.ClassInhHier2DB__ClassToTableRule, 30 * nrOfApplications)
			.setMax(API_ClassInhHier2DB.ClassInhHier2DB__SubClassToTableRule, 30 * nrOfApplications)
			.setMax(API_ClassInhHier2DB.ClassInhHier2DB__AttributeToColumnRule, 80 * nrOfApplications);
		});
		
		for (int j=0; j<nrOfIterations; j++) {
			try {
				testGenApp.run();
				builder.deleteAllCorrs();
				builder.clearModel(TRG_MODEL_NAME);
				Logger.getRootLogger().setLevel(Level.INFO);
				logger.info("FWD_OPT, Iteration: " + j + ". Start new configuration: " + nrOfApplications * 1000 + " elements...");
				testFWD_OPTApp.run();
				Logger.getRootLogger().setLevel(Level.OFF);
				clearDB();
			}
			catch (Exception e) {
				logger.info("FWD_OPT, Iteration: " + j + ", configuration: " + nrOfApplications * 1000 + " elements failed.");
			}
		}
	}
	
	public void runTestForRandomModelsBWD_OPT(int nrOfApplications) throws Exception {
		var testBWD_OPTApp = new ClassInhHier2DB_BWD_OPT_TEST(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testGenApp = new ClassInhHier2DB_GEN_TEST((scheduler) -> {
			scheduler.setMax(API_ClassInhHier2DB.ClassInhHier2DB__PackageToDatabaseRule, 10 * nrOfApplications)
			.setMax(API_ClassInhHier2DB.ClassInhHier2DB__ClassToTableRule, 30 * nrOfApplications)
			.setMax(API_ClassInhHier2DB.ClassInhHier2DB__SubClassToTableRule, 30 * nrOfApplications)
			.setMax(API_ClassInhHier2DB.ClassInhHier2DB__AttributeToColumnRule, 80 * nrOfApplications);
		});
		
		for (int j=0; j<nrOfIterations; j++) {
			try {
				testGenApp.run();
				builder.deleteAllCorrs();
				builder.clearModel(SRC_MODEL_NAME);
				Logger.getRootLogger().setLevel(Level.INFO);
				logger.info("BWD_OPT, Iteration: " + j + ". Start new configuration: " + nrOfApplications * 1000 + " elements...");
				testBWD_OPTApp.run();
				Logger.getRootLogger().setLevel(Level.OFF);
				clearDB();
			}
			catch (Exception e) {
				logger.info("BWD_OPT, Iteration: " + j + ", configuration: " + nrOfApplications * 1000 + " elements failed.");
			}
		}
	}
	
//	public void runTestForRandomModelsMI(int nrOfApplications, int nrOfConflicts) throws Exception {
//		var testMIApp = new ClassInhHier2DB_MI_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
//		
//		var testGenApp = new ClassInhHier2DB_GEN_TEST((scheduler) -> {
//			scheduler.setMax(API_ClassInhHier2DB.ClassInhHier2DB__ClazzToDocRule, 10 * nrOfApplications)
//			.setMax(API_ClassInhHier2DB.ClassInhHier2DB__SubClazzToSubDocRule, 10 * nrOfApplications)
//			.setMax(API_ClassInhHier2DB.ClassInhHier2DB__MethodToEntryRule, 40 * nrOfApplications)
//			.setMax(API_ClassInhHier2DB.ClassInhHier2DB__FieldToEntryRule, 40 * nrOfApplications)
//			.setMax(API_ClassInhHier2DB.ClassInhHier2DB__AddParameterRule, 40 * nrOfApplications)
//			.setMax(API_ClassInhHier2DB.ClassInhHier2DB__AddGlossaryRule, 1)
//			.setMax(API_ClassInhHier2DB.ClassInhHier2DB__AddGlossaryEntryRule, 40 * nrOfApplications)
//			.setMax(API_ClassInhHier2DB.ClassInhHier2DB__LinkGlossaryEntryRule, 40 * nrOfApplications);
//		});
//		
//		for (int j=0; j<nrOfIterations; j++) {
//			try {
//				testGenApp.run();
//				for (int i=0; i < nrOfConflicts; i++) {
//					api.getRule_CreateDeleteConflict().rule().apply();
//					api.getRule_MoveMoveConflict().rule().apply();
//					api.getRule_MoveDeleteConflict().rule().apply();
//				}
//				Logger.getRootLogger().setLevel(Level.INFO);
//				logger.info("MI, Iteration: " + j + ". Start new configuration: " + nrOfApplications * 1000 + " elements...");
//				testMIApp.run(solver);
//				Logger.getRootLogger().setLevel(Level.OFF);
//				clearDB();
//			}
//			catch (Exception e) {
//				logger.info("MI, Iteration: " + j + ", configuration: " + nrOfApplications * 1000 + " elements failed.");
//			}
//		}
//	}
	
	@Ignore("Performance tests are not part of the test suite, enable it for testing performance")
	//@Test
	public void testPerformanceForRandomModels() throws Exception {
		for (int i : new int[]{/*1,2,5,10,20,50*/100}) {
			Logger.getRootLogger().setLevel(Level.OFF);
			runTestForRandomModelsCO(i);
			runTestForRandomModelsCC(i);
			runTestForRandomModelsFWD_OPT(i);
			runTestForRandomModelsBWD_OPT(i);
			//runTestForRandomModelsMI(i,i);
		}
	}
}