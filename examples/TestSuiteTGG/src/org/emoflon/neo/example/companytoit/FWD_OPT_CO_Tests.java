package org.emoflon.neo.example.companytoit;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.emoflon.neo.api.companytoit.run.CompanyToIT_CO_Run;
import org.emoflon.neo.api.companytoit.run.CompanyToIT_FWD_OPT_Run;
import org.emoflon.neo.api.testsuitetgg.org.emoflon.neo.example.companytoit.API_CompanyToITTriplesForTesting;
import org.emoflon.neo.emsl.eMSL.Metamodel;
import org.emoflon.neo.emsl.eMSL.Model;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;

public class FWD_OPT_CO_Tests extends ENeoTest {

	private API_CompanyToITTriplesForTesting api = new API_CompanyToITTriplesForTesting(builder);

	private void runTest(Model srcModel, Model trgModel, String srcName, String trgName) throws Exception {
		builder.exportEMSLEntityToNeo4j(srcModel);
		var metamodels = builder.collectDependentMetamodels(trgModel);
		for (Metamodel m : metamodels) 
			builder.exportEMSLEntityToNeo4j(m);
		new CompanyToIT_FWD_OPT_Run(srcName, trgName, solver).run();
		assertTrue(new CompanyToIT_CO_Run(srcName, trgName, solver).runCheckOnly().isConsistent());
	}

	@Test
	public void testTriple1() throws Exception {
		runTest(api.getModel_ConsistentSrc1(), api.getModel_ConsistentTrg1(), "ConsistentSrc1", "Target1");
	}

	@Test
	public void testTriple2() throws Exception {
		runTest(api.getModel_ConsistentSrc2(), api.getModel_ConsistentTrg2(), "ConsistentSrc2", "Target2");
	}

	@Test
	public void testTriple3() throws Exception {
		runTest(api.getModel_ConsistentSrc3(), api.getModel_ConsistentTrg3(), "ConsistentSrc3", "Target3");
	}

	@Test
	public void testTriple4() throws Exception {
		runTest(api.getModel_ConsistentSrc4(), api.getModel_ConsistentTrg4(), "ConsistentSrc4", "Target4");
	}
}
