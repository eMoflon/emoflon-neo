package org.emoflon.neo.example.facebooktoinstagram;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.emoflon.neo.api.org.emoflon.neo.example.facebooktoinstagram.API_FacebookToInstagramTriplesForTesting;
import org.emoflon.neo.emsl.eMSL.Model;
import org.emoflon.neo.example.ENeoTest;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import Transformations.run.FacebookToInstagramFASE_CO_Run;
import Transformations.run.FacebookToInstagramFASE_FWD_Run;

public class FWD_CO_Tests extends ENeoTest {

	private API_FacebookToInstagramTriplesForTesting api = new API_FacebookToInstagramTriplesForTesting(builder);

	private void runTest(Model srcModel, String srcName, String trgName) throws Exception {
		builder.exportEMSLEntityToNeo4j(srcModel);
		new FacebookToInstagramFASE_FWD_Run(srcName, trgName).run();
		assertTrue(new FacebookToInstagramFASE_CO_Run(srcName, trgName, solver).runCheckOnly().isConsistent());
	}

	@Test
	public void testTriple1() throws Exception {
		runTest(api.getModel_ConsistentSrc1(), "ConsistentSrc1", "Target1");
	}

	@Test
	public void testTriple2() throws Exception {
		runTest(api.getModel_ConsistentSrc2(), "ConsistentSrc2", "Target2");
	}

	@Ignore
	// Doesn't terminate because RequestFrienship is an ignore rule for FWD
	public void testTriple3() throws Exception {
		runTest(api.getModel_ConsistentSrc3(), "ConsistentSrc3", "Target3");
	}
}
