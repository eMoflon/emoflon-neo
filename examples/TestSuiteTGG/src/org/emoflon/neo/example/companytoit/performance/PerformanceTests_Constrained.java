package org.emoflon.neo.example.companytoit.performance;

import static org.emoflon.neo.api.companytoit_constrained.run.CompanyToIT_Constrained_GEN_Run.SRC_MODEL_NAME;
import static org.emoflon.neo.api.companytoit_constrained.run.CompanyToIT_Constrained_GEN_Run.TRG_MODEL_NAME;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.companytoit_constrained.API_CompanyToIT_Constrained;
import org.emoflon.neo.api.companytoit_constrained.run.CompanyToIT_Constrained_BWD_OPT_Run;
import org.emoflon.neo.api.companytoit_constrained.run.CompanyToIT_Constrained_FWD_OPT_Run;

import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;
import org.emoflon.neo.api.companytoit_constrained.run.CompanyToIT_Constrained_CC_Run;
import org.emoflon.neo.api.companytoit_constrained.run.CompanyToIT_Constrained_CO_Run;

public class PerformanceTests_Constrained extends ENeoTest {
	
	private static final int nrOfIterations = 5;
	
	public void runTestForRandomModelsCO(int nrOfApplications) {
		var testCOApp = new CompanyToIT_Constrained_CO_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testGenApp = new CompanyToIT_GEN_TEST((scheduler) -> {
			scheduler.setMax(API_CompanyToIT_Constrained.CompanyToIT_Constrained__CompanyToITRule, 10 * nrOfApplications)
			.setMax(API_CompanyToIT_Constrained.CompanyToIT_Constrained__AdminToRouterRule, 20 * nrOfApplications)
			.setMax(API_CompanyToIT_Constrained.CompanyToIT_Constrained__EmployeeToPCRule, 40 * nrOfApplications)
			.setMax(API_CompanyToIT_Constrained.CompanyToIT_Constrained__EmployeeToLaptopRule, 40 * nrOfApplications);
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
	
	public void runTestForRandomModelsCC(int nrOfApplications) {
		var testCCApp = new CompanyToIT_Constrained_CC_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testGenApp = new CompanyToIT_GEN_TEST((scheduler) -> {
			scheduler.setMax(API_CompanyToIT_Constrained.CompanyToIT_Constrained__CompanyToITRule, 10 * nrOfApplications)
			.setMax(API_CompanyToIT_Constrained.CompanyToIT_Constrained__AdminToRouterRule, 20 * nrOfApplications)
			.setMax(API_CompanyToIT_Constrained.CompanyToIT_Constrained__EmployeeToPCRule, 40 * nrOfApplications)
			.setMax(API_CompanyToIT_Constrained.CompanyToIT_Constrained__EmployeeToLaptopRule, 40 * nrOfApplications);
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
	
	public void runTestForRandomModelsFWD_OPT(int nrOfApplications) {
		var testFWD_OPTApp = new CompanyToIT_Constrained_FWD_OPT_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testGenApp = new CompanyToIT_GEN_TEST((scheduler) -> {
			scheduler.setMax(API_CompanyToIT_Constrained.CompanyToIT_Constrained__CompanyToITRule, 10 * nrOfApplications)
			.setMax(API_CompanyToIT_Constrained.CompanyToIT_Constrained__AdminToRouterRule, 20 * nrOfApplications)
			.setMax(API_CompanyToIT_Constrained.CompanyToIT_Constrained__EmployeeToPCRule, 40 * nrOfApplications)
			.setMax(API_CompanyToIT_Constrained.CompanyToIT_Constrained__EmployeeToLaptopRule, 40 * nrOfApplications);
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
	
	public void runTestForRandomModelsBWD_OPT(int nrOfApplications) {
		var testBWD_OPTApp = new CompanyToIT_Constrained_BWD_OPT_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testGenApp = new CompanyToIT_GEN_TEST((scheduler) -> {
			scheduler.setMax(API_CompanyToIT_Constrained.CompanyToIT_Constrained__CompanyToITRule, 10 * nrOfApplications)
			.setMax(API_CompanyToIT_Constrained.CompanyToIT_Constrained__AdminToRouterRule, 20 * nrOfApplications)
			.setMax(API_CompanyToIT_Constrained.CompanyToIT_Constrained__EmployeeToPCRule, 40 * nrOfApplications)
			.setMax(API_CompanyToIT_Constrained.CompanyToIT_Constrained__EmployeeToLaptopRule, 40 * nrOfApplications);
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
//		var testMIApp = new CompanyToIT_Constrained_MI_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
//		
//		var testGenApp = new CompanyToIT_GEN_TEST((scheduler) -> {
//			scheduler.setMax(API_Schema.CompanyToIT_Constrained__ClazzToDocRule, 10 * nrOfApplications)
//			.setMax(API_Schema.CompanyToIT_Constrained__SubClazzToSubDocRule, 10 * nrOfApplications)
//			.setMax(API_Schema.CompanyToIT_Constrained__MethodToEntryRule, 40 * nrOfApplications)
//			.setMax(API_Schema.CompanyToIT_Constrained__FieldToEntryRule, 40 * nrOfApplications)
//			.setMax(API_Schema.CompanyToIT_Constrained__AddParameterRule, 40 * nrOfApplications)
//			.setMax(API_Schema.CompanyToIT_Constrained__AddGlossaryRule, 1)
//			.setMax(API_Schema.CompanyToIT_Constrained__AddGlossaryEntryRule, 40 * nrOfApplications)
//			.setMax(API_Schema.CompanyToIT_Constrained__LinkGlossaryEntryRule, 40 * nrOfApplications);
//		});
//		
//		for (int j=0; j<nrOfIterations; j++) {
//			testGenApp.run();
//			for (int i=0; i < nrOfConflicts; i++) {
//				api.getRule_CreateDeleteConflict().rule().apply();
//				api.getRule_MoveMoveConflict().rule().apply();
//				api.getRule_MoveDeleteConflict().rule().apply();
//			}
//			Logger.getRootLogger().setLevel(Level.INFO);
//			logger.info("MI, Iteration: " + j + ". Start new configuration: " + nrOfApplications * 1000 + " elements...");
//			testMIApp.run(solver);
//			Logger.getRootLogger().setLevel(Level.OFF);
//			clearDB();
//		}
//	}
	
	//@Ignore("Performance tests are not part of the test suite, enable it for testing performance")
	@Test
	public void testPerformanceForRandomModels() {
		for (int i : new int[]{/*1,2,5,10,20,50*/100}) {
			Logger.getRootLogger().setLevel(Level.OFF);
			runTestForRandomModelsCO(i);
			//runTestForRandomModelsCC(i);
			runTestForRandomModelsFWD_OPT(i);
			runTestForRandomModelsBWD_OPT(i);
			//runTestForRandomModelsMI(i,i);
		}
	}
}