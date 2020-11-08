package org.emoflon.neo.example.companytoit;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.emoflon.neo.api.companytoit.run.CompanyToIT_BWD_Run;
import org.emoflon.neo.api.companytoit.run.CompanyToIT_CO_Run;
import org.emoflon.neo.api.testsuitetgg.org.emoflon.neo.example.companytoit.API_CompanyToITTriplesForTesting;
import org.emoflon.neo.emsl.eMSL.Model;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;

public class BWD_CO_Tests extends ENeoTest {

	private API_CompanyToITTriplesForTesting api = new API_CompanyToITTriplesForTesting(builder);

	private void runTest(Model trgModel, String srcName, String trgName) throws Exception {
		builder.exportEMSLEntityToNeo4j(trgModel);
		new CompanyToIT_BWD_Run(srcName, trgName).run();
		assertTrue(new CompanyToIT_CO_Run(srcName, trgName, solver).runCheckOnly().isConsistent());
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

	@Test
	public void testTriple4() throws Exception {
		runTest(api.getModel_ConsistentTrg4(), "Source4", "ConsistentTrg4");
	}
}
