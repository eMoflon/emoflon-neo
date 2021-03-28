package org.emoflon.neo.example.familiestopersons;

import static org.emoflon.neo.api.familiestopersons_constrained.run.FamiliesToPersons_Constrained_GEN_Run.SRC_MODEL_NAME;
import static org.emoflon.neo.api.familiestopersons_constrained.run.FamiliesToPersons_Constrained_GEN_Run.TRG_MODEL_NAME;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.familiestopersons_constrained.API_DaughterOfExistingFamilyToFemale;
import org.emoflon.neo.api.familiestopersons_constrained.API_DaughterToFemale;
import org.emoflon.neo.api.familiestopersons_constrained.API_Families2Persons;
import org.emoflon.neo.api.familiestopersons_constrained.API_FatherOfExistingFamilyToMale;
import org.emoflon.neo.api.familiestopersons_constrained.API_FatherToMale;
import org.emoflon.neo.api.familiestopersons_constrained.API_MotherOfExistingFamilyToFemale;
import org.emoflon.neo.api.familiestopersons_constrained.API_MotherToFemale;
import org.emoflon.neo.api.familiestopersons_constrained.API_SonOfExistingFamilyToMale;
import org.emoflon.neo.api.familiestopersons_constrained.API_SonToMale;
import org.emoflon.neo.example.ENeoTest;

import org.emoflon.neo.api.familiestopersons_constrained.run.FamiliesToPersons_Constrained_BWD_OPT_Run;
import org.emoflon.neo.api.familiestopersons_constrained.run.FamiliesToPersons_Constrained_CC_Run;
import org.emoflon.neo.api.familiestopersons_constrained.run.FamiliesToPersons_Constrained_CO_Run;
import org.emoflon.neo.api.familiestopersons_constrained.run.FamiliesToPersons_Constrained_FWD_OPT_Run;
import org.junit.Ignore;

public class ConstraintPerformanceTest extends ENeoTest{
public void runTestForFixedSize(int nrOfApplications, int nrOfIterations) throws Exception {
		
		Logger.getRootLogger().setLevel(Level.INFO);
		var testCOApp = new FamiliesToPersons_Constrained_CO_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testCCApp = new FamiliesToPersons_Constrained_CC_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testFWD_OPTApp = new FamiliesToPersons_Constrained_FWD_OPT_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testBWD_OPTApp = new FamiliesToPersons_Constrained_BWD_OPT_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		
		var testGenApp = new FamiliesToPersons_GEN_TEST((scheduler) -> {
			scheduler.setMax(API_DaughterOfExistingFamilyToFemale.FamiliesToPersons_Constrained__DaughterOfExistingFamilyToFemale, 2 * nrOfApplications)
			.setMax(API_DaughterToFemale.FamiliesToPersons_Constrained__DaughterToFemale, 2 * nrOfApplications)
			.setMax(API_Families2Persons.FamiliesToPersons_Constrained__Families2Persons, 1 * nrOfApplications)
			.setMax(API_FatherOfExistingFamilyToMale.FamiliesToPersons_Constrained__FatherOfExistingFamilyToMale, 1 * nrOfApplications)
			.setMax(API_FatherToMale.FamiliesToPersons_Constrained__FatherToMale, 1 * nrOfApplications)
			.setMax(API_MotherOfExistingFamilyToFemale.FamiliesToPersons_Constrained__MotherOfExistingFamilyToFemale, 1 * nrOfApplications)
			.setMax(API_MotherToFemale.FamiliesToPersons_Constrained__MotherToFemale, 1 * nrOfApplications)		
			.setMax(API_SonOfExistingFamilyToMale.FamiliesToPersons_Constrained__SonOfExistingFamilyToMale, 2 * nrOfApplications)
			.setMax(API_SonToMale.FamiliesToPersons_Constrained__SonToMale, 2 * nrOfApplications);
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
	
	@Ignore("Performance tests are not part of the test suite, enable it for testing performance")
	public void testPerformance() throws Exception {
		for (int i : new int[]{10, 20, 50, 100, 150, 200}) {
			Logger.getRootLogger().setLevel(Level.OFF);
			runTestForFixedSize(i, 9);
		}
	}

}
