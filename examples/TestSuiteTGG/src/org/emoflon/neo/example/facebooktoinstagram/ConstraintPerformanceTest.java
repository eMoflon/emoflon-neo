package org.emoflon.neo.example.facebooktoinstagram;

import static org.emoflon.neo.api.facebooktoinstagramfase.run.FacebookToInstagramFASE_GEN_Run.SRC_MODEL_NAME;
import static org.emoflon.neo.api.facebooktoinstagramfase.run.FacebookToInstagramFASE_GEN_Run.TRG_MODEL_NAME;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.facebooktoinstagramfase.API_Transformations;
import org.emoflon.neo.example.ENeoTest;

import org.emoflon.neo.api.facebooktoinstagramfase.run.FacebookToInstagramFASE_BWD_OPT_Run;
import org.emoflon.neo.api.facebooktoinstagramfase.run.FacebookToInstagramFASE_CC_Run;
import org.emoflon.neo.api.facebooktoinstagramfase.run.FacebookToInstagramFASE_CO_Run;
import org.emoflon.neo.api.facebooktoinstagramfase.run.FacebookToInstagramFASE_FWD_OPT_Run;
import org.junit.Ignore;

public class ConstraintPerformanceTest extends ENeoTest {
public void runTestForFixedSize(int nrOfApplications, int nrOfIterations) throws Exception {
		
		Logger.getRootLogger().setLevel(Level.INFO);
		var testCOApp = new FacebookToInstagramFASE_CO_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testCCApp = new FacebookToInstagramFASE_CC_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testFWD_OPTApp = new FacebookToInstagramFASE_FWD_OPT_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		var testBWD_OPTApp = new FacebookToInstagramFASE_BWD_OPT_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		
		var testGenApp = new FacebookToInstagram_GEN_TEST((scheduler) -> {
			scheduler.setMax(API_Transformations.FacebookToInstagramFASE__NetworkToNetwork, 1 * nrOfApplications)
			.setMax(API_Transformations.FacebookToInstagramFASE__UserToUser, 2 * nrOfApplications)
			.setMax(API_Transformations.FacebookToInstagramFASE__RequestFriendship, 4 * nrOfApplications)
			.setMax(API_Transformations.FacebookToInstagramFASE__AcceptFriendship, 4 * nrOfApplications);
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
			runTestForFixedSize(i, 7);
		}
	}

}
