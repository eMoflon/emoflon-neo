package org.emoflon.neo.example.companytoit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.org.emoflon.neo.example.companytoit.API_CompanyToITTriplesForTesting;
import org.emoflon.neo.emsl.eMSL.Model;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;

import run.CompanyToIT_CO_Run;

public class DirectCOTests extends ENeoTest {

	public void testConsistentTriple(Model triple) throws Exception {
		var testCOApp = new CompanyToIT_CO_Run();
		var builder = API_Common.createBuilder();
		builder.exportEMSLEntityToNeo4j(triple);
		assertTrue(testCOApp.runCheckOnly());
	}

	public void testInConsistentTriple(Model triple) throws Exception {
		var testCOApp = new CompanyToIT_CO_Run();
		var builder = API_Common.createBuilder();
		builder.exportEMSLEntityToNeo4j(triple);
		assertFalse(testCOApp.runCheckOnly());
	}

	@Test
	public void testMissingCEO() throws Exception {
		testInConsistentTriple(new API_CompanyToITTriplesForTesting(builder).getModel_MissingCEO());
	}

	@Test
	public void testWrongNames() throws Exception {
		testInConsistentTriple(new API_CompanyToITTriplesForTesting(builder).getModel_WrongNames());
	}

	@Test
	public void testConsistentTriple() throws Exception {
		testConsistentTriple(new API_CompanyToITTriplesForTesting(builder).getModel_ConsistentTriple());
	}

	@Test
	public void testExtraCEO() throws Exception {
		testInConsistentTriple(new API_CompanyToITTriplesForTesting(builder).getModel_ExtraCEO());
	}
}
