package org.emoflon.neo.example.facebooktoinstagram;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.emoflon.neo.api.facebooktoinstagramfase.run.FacebookToInstagramFASE_CO_Run;
import org.emoflon.neo.api.facebooktoinstagramfase.run.FacebookToInstagramFASE_FWD_OPT_Run;
import org.emoflon.neo.api.testsuitetgg.org.emoflon.neo.example.facebooktoinstagram.API_FacebookToInstagramTriplesForTesting;
import org.emoflon.neo.emsl.eMSL.Metamodel;
import org.emoflon.neo.emsl.eMSL.Model;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;

public class FWD_OPT_CO_Tests extends ENeoTest {

	private API_FacebookToInstagramTriplesForTesting api = new API_FacebookToInstagramTriplesForTesting(builder);

	private void runTest(Model srcModel, Model trgModel, String srcName, String trgName) throws Exception {
		builder.exportEMSLEntityToNeo4j(srcModel);
		var metamodels = builder.collectDependentMetamodels(trgModel);
		for (Metamodel m : metamodels) 
			builder.exportEMSLEntityToNeo4j(m);
		new FacebookToInstagramFASE_FWD_OPT_Run(srcName, trgName, solver).run();
		assertTrue(new FacebookToInstagramFASE_CO_Run(srcName, trgName, solver).runCheckOnly().isConsistent());
	}

	@Test
	public void testTriple1() throws Exception {
		runTest(api.getModel_ConsistentSrc1(), api.getModel_ConsistentTrg1(), "ConsistentSrc1", "Target1");
	}

	@Test
	public void testTriple2() throws Exception {
		runTest(api.getModel_ConsistentSrc2(), api.getModel_ConsistentTrg2(), "ConsistentSrc2", "Target2");
	}

	// Doesn't terminate because RequestFrienship is an ignore rule for FWD_OPT
	public void testTriple3() throws Exception {
		runTest(api.getModel_ConsistentSrc3(), api.getModel_ConsistentTrg3(), "ConsistentSrc3", "Target3");
	}
}
