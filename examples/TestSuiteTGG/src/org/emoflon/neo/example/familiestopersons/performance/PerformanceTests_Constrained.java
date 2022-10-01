package org.emoflon.neo.example.familiestopersons.performance;

import static org.emoflon.neo.api.familiestopersons.run.FamiliesToPersons_GEN_Run.SRC_MODEL_NAME;
import static org.emoflon.neo.api.familiestopersons.run.FamiliesToPersons_GEN_Run.TRG_MODEL_NAME;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.familiestopersons.API_MotherToFemale;
import org.emoflon.neo.api.familiestopersons.API_MotherOfExistingFamilyToFemale;
import org.emoflon.neo.api.familiestopersons.API_FatherToMale;
import org.emoflon.neo.api.familiestopersons.API_FatherOfExistingFamilyToMale;
import org.emoflon.neo.api.familiestopersons.API_DaughterToFemale;

import org.emoflon.neo.api.familiestopersons.API_DaughterOfExistingFamilyToFemale;
import org.emoflon.neo.api.familiestopersons.API_SonToMale;
import org.emoflon.neo.api.familiestopersons.API_SonOfExistingFamilyToMale;
import org.emoflon.neo.api.familiestopersons.API_Families2Persons;
import org.emoflon.neo.example.ENeoTest;
import org.junit.Ignore;
import org.emoflon.neo.api.familiestopersons.run.FamiliesToPersons_CC_Run;
import org.emoflon.neo.api.familiestopersons.run.FamiliesToPersons_CO_Run;
import org.emoflon.neo.api.familiestopersons.run.FamiliesToPersons_BWD_OPT_Run;
import org.emoflon.neo.api.familiestopersons.run.FamiliesToPersons_FWD_OPT_Run;

public class PerformanceTests_Constrained extends ENeoTest {
	
	private static final int nrOfIterations = 5;
	public void runTestForRandomModelsCO(int nrOfApplications) {
		var testCOApp = new FamiliesToPersons_CO_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testGenApp = new FamiliesToPersons_GEN_TEST((scheduler) -> {
			scheduler.setMax(API_Families2Persons.FamiliesToPersons__Families2Persons, 10 * nrOfApplications)
			.setMax(API_MotherToFemale.FamiliesToPersons__MotherToFemale, 10 * nrOfApplications)
			.setMax(API_MotherOfExistingFamilyToFemale.FamiliesToPersons__MotherOfExistingFamilyToFemale, 20 * nrOfApplications)
			.setMax(API_FatherToMale.FamiliesToPersons__FatherToMale, 10 * nrOfApplications)
			.setMax(API_FatherOfExistingFamilyToMale.FamiliesToPersons__FatherOfExistingFamilyToMale, 20 * nrOfApplications)
			.setMax(API_DaughterToFemale.FamiliesToPersons__DaughterToFemale, 10 * nrOfApplications)
			.setMax(API_DaughterOfExistingFamilyToFemale.FamiliesToPersons__DaughterOfExistingFamilyToFemale, 20 * nrOfApplications)
			.setMax(API_SonToMale.FamiliesToPersons__SonToMale, 10 * nrOfApplications)
			.setMax(API_SonOfExistingFamilyToMale.FamiliesToPersons__SonOfExistingFamilyToMale, 20 * nrOfApplications);
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
		var testCCApp = new FamiliesToPersons_CC_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testGenApp = new FamiliesToPersons_GEN_TEST((scheduler) -> {
			scheduler.setMax(API_Families2Persons.FamiliesToPersons__Families2Persons, 10 * nrOfApplications)
			.setMax(API_MotherToFemale.FamiliesToPersons__MotherToFemale, 10 * nrOfApplications)
			.setMax(API_MotherOfExistingFamilyToFemale.FamiliesToPersons__MotherOfExistingFamilyToFemale, 20 * nrOfApplications)
			.setMax(API_FatherToMale.FamiliesToPersons__FatherToMale, 10 * nrOfApplications)
			.setMax(API_FatherOfExistingFamilyToMale.FamiliesToPersons__FatherOfExistingFamilyToMale, 20 * nrOfApplications)
			.setMax(API_DaughterToFemale.FamiliesToPersons__DaughterToFemale, 10 * nrOfApplications)
			.setMax(API_DaughterOfExistingFamilyToFemale.FamiliesToPersons__DaughterOfExistingFamilyToFemale, 20 * nrOfApplications)
			.setMax(API_SonToMale.FamiliesToPersons__SonToMale, 10 * nrOfApplications)
			.setMax(API_SonOfExistingFamilyToMale.FamiliesToPersons__SonOfExistingFamilyToMale, 20 * nrOfApplications);
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
		var testFWD_OPTApp = new FamiliesToPersons_FWD_OPT_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testGenApp = new FamiliesToPersons_GEN_TEST((scheduler) -> {
			scheduler.setMax(API_Families2Persons.FamiliesToPersons__Families2Persons, 10 * nrOfApplications)
			.setMax(API_MotherToFemale.FamiliesToPersons__MotherToFemale, 10 * nrOfApplications)
			.setMax(API_MotherOfExistingFamilyToFemale.FamiliesToPersons__MotherOfExistingFamilyToFemale, 20 * nrOfApplications)
			.setMax(API_FatherToMale.FamiliesToPersons__FatherToMale, 10 * nrOfApplications)
			.setMax(API_FatherOfExistingFamilyToMale.FamiliesToPersons__FatherOfExistingFamilyToMale, 20 * nrOfApplications)
			.setMax(API_DaughterToFemale.FamiliesToPersons__DaughterToFemale, 10 * nrOfApplications)
			.setMax(API_DaughterOfExistingFamilyToFemale.FamiliesToPersons__DaughterOfExistingFamilyToFemale, 20 * nrOfApplications)
			.setMax(API_SonToMale.FamiliesToPersons__SonToMale, 10 * nrOfApplications)
			.setMax(API_SonOfExistingFamilyToMale.FamiliesToPersons__SonOfExistingFamilyToMale, 20 * nrOfApplications);
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
		var testBWD_OPTApp = new FamiliesToPersons_BWD_OPT_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testGenApp = new FamiliesToPersons_GEN_TEST((scheduler) -> {
			scheduler.setMax(API_Families2Persons.FamiliesToPersons__Families2Persons, 10 * nrOfApplications)
			.setMax(API_MotherToFemale.FamiliesToPersons__MotherToFemale, 10 * nrOfApplications)
			.setMax(API_MotherOfExistingFamilyToFemale.FamiliesToPersons__MotherOfExistingFamilyToFemale, 20 * nrOfApplications)
			.setMax(API_FatherToMale.FamiliesToPersons__FatherToMale, 10 * nrOfApplications)
			.setMax(API_FatherOfExistingFamilyToMale.FamiliesToPersons__FatherOfExistingFamilyToMale, 20 * nrOfApplications)
			.setMax(API_DaughterToFemale.FamiliesToPersons__DaughterToFemale, 10 * nrOfApplications)
			.setMax(API_DaughterOfExistingFamilyToFemale.FamiliesToPersons__DaughterOfExistingFamilyToFemale, 20 * nrOfApplications)
			.setMax(API_SonToMale.FamiliesToPersons__SonToMale, 10 * nrOfApplications)
			.setMax(API_SonOfExistingFamilyToMale.FamiliesToPersons__SonOfExistingFamilyToMale, 20 * nrOfApplications);
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
//		var testMIApp = new FamiliesToPersons_MI_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
//		
//		var testGenApp = new FamiliesToPersons_GEN_TEST((scheduler) -> {
//			scheduler.setMax(API_Schema.FamiliesToPersons__ClazzToDocRule, 10 * nrOfApplications)
//			.setMax(API_Schema.FamiliesToPersons__SubClazzToSubDocRule, 10 * nrOfApplications)
//			.setMax(API_Schema.FamiliesToPersons__MethodToEntryRule, 40 * nrOfApplications)
//			.setMax(API_Schema.FamiliesToPersons__FieldToEntryRule, 40 * nrOfApplications)
//			.setMax(API_Schema.FamiliesToPersons__AddParameterRule, 40 * nrOfApplications)
//			.setMax(API_Schema.FamiliesToPersons__AddGlossaryRule, 1)
//			.setMax(API_Schema.FamiliesToPersons__AddGlossaryEntryRule, 40 * nrOfApplications)
//			.setMax(API_Schema.FamiliesToPersons__LinkGlossaryEntryRule, 40 * nrOfApplications);
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
	
	@Ignore("Performance tests are not part of the test suite, enable it for testing performance")
	//@Test
	public void testPerformanceForRandomModels() {
		for (int i : new int[]{/*1,2,5,10,20,50*/100}) {
			Logger.getRootLogger().setLevel(Level.OFF);
			runTestForRandomModelsCO(i);
			//runTestForRandomModelsCC(i);
			runTestForRandomModelsFWD_OPT(i);
			//runTestForRandomModelsBWD_OPT(i);
			//runTestForRandomModelsMI(i,i);
		}
	}
}