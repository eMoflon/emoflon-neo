package org.emoflon.neo.example.companytoit;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.emoflon.neo.api.org.emoflon.neo.example.companytoit.API_CompanyToITTriplesForTesting;
import org.emoflon.neo.emsl.eMSL.Model;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;

import CompanyToIT.run.CompanyToIT_CO_Run;
import CompanyToIT.run.CompanyToIT_FWD_Run;

public class FWD_CO_Tests extends ENeoTest {

	private API_CompanyToITTriplesForTesting api = new API_CompanyToITTriplesForTesting(builder);

	private void runTest(Model srcModel, String srcName, String trgName) throws Exception {
		builder.exportEMSLEntityToNeo4j(srcModel);
		new CompanyToIT_FWD_Run(srcName, trgName).run();
		assertTrue(new CompanyToIT_CO_Run(srcName, trgName).runCheckOnly().isConsistent());
	}

	@Test
	public void testTriple1() throws Exception {
		runTest(api.getModel_ConsistentSrc1(), "ConsistentSrc1", "Target1");
	}

	@Test
	public void testTriple2() throws Exception {
		runTest(api.getModel_ConsistentSrc2(), "ConsistentSrc2", "Target2");
	}

	@Test
	public void testTriple3() throws Exception {
		runTest(api.getModel_ConsistentSrc3(), "ConsistentSrc3", "Target3");
	}

	@Test
	public void testTriple4() throws Exception {
		runTest(api.getModel_ConsistentSrc4(), "ConsistentSrc4", "Target4");
	}
}
