package org.emoflon.neo.example.companytoit;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.emoflon.neo.api.org.emoflon.neo.example.companytoit.API_CompanyToITTriplesForTesting;
import org.emoflon.neo.emsl.eMSL.Metamodel;
import org.emoflon.neo.emsl.eMSL.Model;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;

import CompanyToIT.run.CompanyToIT_BWD_OPT_Run;
import CompanyToIT.run.CompanyToIT_CO_Run;

public class BWD_OPT_CO_Tests extends ENeoTest {

	private API_CompanyToITTriplesForTesting api = new API_CompanyToITTriplesForTesting(builder);

	private void runTest(Model srcModel, Model trgModel, String srcName, String trgName) throws Exception {
		builder.exportEMSLEntityToNeo4j(trgModel);
		var metamodels = builder.collectDependentMetamodels(srcModel);
		for (Metamodel m : metamodels) 
			builder.exportEMSLEntityToNeo4j(m);
		new CompanyToIT_BWD_OPT_Run(srcName, trgName).run();
		assertTrue(new CompanyToIT_CO_Run(srcName, trgName).runCheckOnly().isConsistent());
	}

	@Test
	public void testTriple1() throws Exception {
		runTest(api.getModel_ConsistentSrc1(), api.getModel_ConsistentTrg1(), "Source1", "ConsistentTrg1");
	}

	@Test
	public void testTriple2() throws Exception {
		runTest(api.getModel_ConsistentSrc2(), api.getModel_ConsistentTrg2(), "Source2", "ConsistentTrg2");
	}

	@Test
	public void testTriple3() throws Exception {
		runTest(api.getModel_ConsistentSrc3(), api.getModel_ConsistentTrg3(), "Source3", "ConsistentTrg3");
	}

	@Test
	public void testTriple4() throws Exception {
		runTest(api.getModel_ConsistentSrc4(), api.getModel_ConsistentTrg4(), "Source4", "ConsistentTrg4");
	}
}
