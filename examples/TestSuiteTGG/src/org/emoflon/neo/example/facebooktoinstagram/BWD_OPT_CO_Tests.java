package org.emoflon.neo.example.facebooktoinstagram;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.emoflon.neo.api.org.emoflon.neo.example.facebooktoinstagram.API_FacebookToInstagramTriplesForTesting;
import org.emoflon.neo.emsl.eMSL.Model;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import Transformations.run.FacebookToInstagramGrammar_BWD_OPT_Run;
import Transformations.run.FacebookToInstagramGrammar_CO_Run;

@Disabled("Waiting for fix from Nils")
public class BWD_OPT_CO_Tests extends ENeoTest {

	private API_FacebookToInstagramTriplesForTesting api = new API_FacebookToInstagramTriplesForTesting(builder);

	private void runTest(Model trgModel, String srcName, String trgName) throws Exception {
		builder.exportEMSLEntityToNeo4j(trgModel);
		new FacebookToInstagramGrammar_BWD_OPT_Run(srcName, trgName).run();
		assertTrue(new FacebookToInstagramGrammar_CO_Run(srcName, trgName).runCheckOnly().isConsistent());
	}

	@Test
	public void testTriple1() throws Exception {
		runTest(api.getModel_ConsistentTrg1(), "Source1", "ConsistentTrg1");
	}

	@Test
	public void testTriple2() throws Exception {
		runTest(api.getModel_ConsistentTrg2(), "Source2", "ConsistentTrg2");
	}

	@Test
	public void testTriple3() throws Exception {
		runTest(api.getModel_ConsistentTrg3(), "Source3", "ConsistentTrg3");
	}
}
