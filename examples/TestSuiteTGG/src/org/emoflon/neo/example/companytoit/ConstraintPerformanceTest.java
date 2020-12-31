package org.emoflon.neo.example.companytoit;

import static run.CompanyToIT_GEN_Run.SRC_MODEL_NAME;
import static run.CompanyToIT_GEN_Run.TRG_MODEL_NAME;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.companytoit.API_CompanyToIT;
import org.emoflon.neo.example.ENeoTest;

import org.emoflon.neo.api.companytoit.run.CompanyToIT_BWD_OPT_Run;
import org.emoflon.neo.api.companytoit.run.CompanyToIT_CC_Run;
import org.emoflon.neo.api.companytoit.run.CompanyToIT_CO_Run;
import org.emoflon.neo.api.companytoit.run.CompanyToIT_FWD_OPT_Run;

import org.junit.jupiter.api.Test;;

public class ConstraintPerformanceTest extends ENeoTest{
public void runTestForFixedSize(int nrOfApplications, int nrOfIterations) throws Exception {
		
		Logger.getRootLogger().setLevel(Level.INFO);
		var testCOApp = new CompanyToIT_CO_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testCCApp = new CompanyToIT_CC_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testFWD_OPTApp = new CompanyToIT_FWD_OPT_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testBWD_OPTApp = new CompanyToIT_BWD_OPT_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		
		var testGenApp = new CompanyToIT_GEN_TEST((scheduler) -> {
			scheduler.setMax(API_CompanyToIT.CompanyToIT__CompanyToITRule, 10 * nrOfApplications)
			.setMax(API_CompanyToIT.CompanyToIT__AdminToRouterRule, 10 * nrOfApplications)
			.setMax(API_CompanyToIT.CompanyToIT__EmployeeToPCRule, 40 * nrOfApplications)
			.setMax(API_CompanyToIT.CompanyToIT__EmployeeToLaptopRule, 40 * nrOfApplications);
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
			Logger.getRootLogger().setLevel(Level.INFO);
			runTestForFixedSize(i, 7);
		}
	}

}
