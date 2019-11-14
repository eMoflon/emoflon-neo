package org.emoflon.neo.example.companytoit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.org.emoflon.neo.example.companytoit.API_CompanyToITTriplesForTesting;
import org.emoflon.neo.emsl.eMSL.Model;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;

import run.CompanyToIT_CC_Run;
import run.CompanyToIT_CO_Run;

public class DirectCOAndCCTests extends ENeoTest {

	private void testConsistentTripleCO(Model triple) throws Exception {
		var testCOApp = new CompanyToIT_CO_Run();
		var builder = API_Common.createBuilder();
		builder.exportEMSLEntityToNeo4j(triple);
		assertTrue(testCOApp.runCheckOnly());
	}

	private void testConsistentTripleCC(Model triple) throws Exception {
		var testCCApp = new CompanyToIT_CC_Run();
		builder.exportEMSLEntityToNeo4j(triple);
		builder.deleteAllCorrs();

		assertTrue(testCCApp.runCorrCreation());
	}

	private void testInConsistentTripleCO(Model triple) throws Exception {
		var testCOApp = new CompanyToIT_CO_Run();
		builder.exportEMSLEntityToNeo4j(triple);
		assertFalse(testCOApp.runCheckOnly());
	}

	private void testInConsistentTripleCC(Model triple) throws Exception {
		var testCCApp = new CompanyToIT_CC_Run();
		builder.exportEMSLEntityToNeo4j(triple);
		builder.deleteAllCorrs();
		assertFalse(testCCApp.runCorrCreation());
	}

	@Test
	public void testMissingCEO_CO() throws Exception {
		testInConsistentTripleCO(new API_CompanyToITTriplesForTesting(builder).getModel_MissingCEO());
	}

	@Test
	public void testWrongNames_CO() throws Exception {
		testInConsistentTripleCO(new API_CompanyToITTriplesForTesting(builder).getModel_WrongNames());
	}

	@Test
	public void testConsistentTriple_CO() throws Exception {
		testConsistentTripleCO(new API_CompanyToITTriplesForTesting(builder).getModel_ConsistentTriple());
	}

	@Test
	public void testConsistentTripleWithEmployee_CO() throws Exception {
		testConsistentTripleCO(new API_CompanyToITTriplesForTesting(builder).getModel_ConsistentTripleWithEmployees());
	}

	@Test
	public void testExtraCEO_CO() throws Exception {
		testInConsistentTripleCO(new API_CompanyToITTriplesForTesting(builder).getModel_ExtraCEO());
	}

	@Test
	public void testMissingCEO_CC() throws Exception {
		testInConsistentTripleCC(new API_CompanyToITTriplesForTesting(builder).getModel_MissingCEO());
	}

	@Test
	public void testWrongNames_CC() throws Exception {
		testInConsistentTripleCC(new API_CompanyToITTriplesForTesting(builder).getModel_WrongNames());
	}

	@Test
	public void testConsistentTriple_CC() throws Exception {
		testConsistentTripleCC(new API_CompanyToITTriplesForTesting(builder).getModel_ConsistentTriple());
	}

	@Test
	public void testConsistentTripleWithEmployee_CC() throws Exception {
		testConsistentTripleCC(new API_CompanyToITTriplesForTesting(builder).getModel_ConsistentTripleWithEmployees());
	}

	@Test
	public void testExtraCEO_CC() throws Exception {
		testInConsistentTripleCC(new API_CompanyToITTriplesForTesting(builder).getModel_ExtraCEO());
	}

}
