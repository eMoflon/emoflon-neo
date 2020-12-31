package org.emoflon.neo.example.familiestopersons;

import static org.emoflon.neo.api.familiestopersons.run.FamiliesToPersons_GEN_Run.SRC_MODEL_NAME;
import static org.emoflon.neo.api.familiestopersons.run.FamiliesToPersons_GEN_Run.TRG_MODEL_NAME;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.familiestopersons.API_DaughterOfExistingFamilyToFemale;
import org.emoflon.neo.api.familiestopersons.API_DaughterToFemale;
import org.emoflon.neo.api.familiestopersons.API_Families2Persons;
import org.emoflon.neo.api.familiestopersons.API_FatherOfExistingFamilyToMale;
import org.emoflon.neo.api.familiestopersons.API_FatherToMale;
import org.emoflon.neo.api.familiestopersons.API_MotherOfExistingFamilyToFemale;
import org.emoflon.neo.api.familiestopersons.API_MotherToFemale;
import org.emoflon.neo.api.familiestopersons.API_SonOfExistingFamilyToMale;
import org.emoflon.neo.api.familiestopersons.API_SonToMale;
import org.emoflon.neo.example.ENeoTest;

import org.emoflon.neo.api.familiestopersons.run.FamiliesToPersons_BWD_OPT_Run;
import org.emoflon.neo.api.familiestopersons.run.FamiliesToPersons_CC_Run;
import org.emoflon.neo.api.familiestopersons.run.FamiliesToPersons_CO_Run;
import org.emoflon.neo.api.familiestopersons.run.FamiliesToPersons_FWD_OPT_Run;

import org.junit.jupiter.api.Test;;

public class ConstraintPerformanceTest extends ENeoTest{
public void runTestForFixedSize(int nrOfApplications, int nrOfIterations) throws Exception {
		
		Logger.getRootLogger().setLevel(Level.INFO);
		var testCOApp = new FamiliesToPersons_CO_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testCCApp = new FamiliesToPersons_CC_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testFWD_OPTApp = new FamiliesToPersons_FWD_OPT_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testBWD_OPTApp = new FamiliesToPersons_BWD_OPT_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		
		var testGenApp = new FamiliesToPersons_GEN_TEST((scheduler) -> {
			scheduler.setMax(API_DaughterOfExistingFamilyToFemale.FamiliesToPersons__DaughterOfExistingFamilyToFemale, 2 * nrOfApplications)
			.setMax(API_DaughterToFemale.FamiliesToPersons__DaughterToFemale, 2 * nrOfApplications)
			.setMax(API_Families2Persons.FamiliesToPersons__Families2Persons, 1 * nrOfApplications)
			.setMax(API_FatherOfExistingFamilyToMale.FamiliesToPersons__FatherOfExistingFamilyToMale, 1 * nrOfApplications)
			.setMax(API_FatherToMale.FamiliesToPersons__FatherToMale, 1 * nrOfApplications)
			.setMax(API_MotherOfExistingFamilyToFemale.FamiliesToPersons__MotherOfExistingFamilyToFemale, 1 * nrOfApplications)
			.setMax(API_MotherToFemale.FamiliesToPersons__MotherToFemale, 1 * nrOfApplications)		
			.setMax(API_SonOfExistingFamilyToMale.FamiliesToPersons__SonOfExistingFamilyToMale, 2 * nrOfApplications)
			.setMax(API_SonToMale.FamiliesToPersons__SonToMale, 2 * nrOfApplications);
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
	}
	
	@Test
	public void testPerformance() throws Exception {
		for (int i : new int[]{10, 20, 50, 100, 150, 200}) {
			Logger.getRootLogger().setLevel(Level.OFF);
			runTestForFixedSize(i, 9);
		}
	}

}
