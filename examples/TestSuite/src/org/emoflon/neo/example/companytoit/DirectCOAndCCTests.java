package org.emoflon.neo.example.companytoit;

import org.emoflon.neo.api.org.emoflon.neo.example.companytoit.API_CompanyToITTriplesForTesting;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;

public class DirectCOAndCCTests extends ENeoTest {

	@Test
	public void testMissingCEO_CO() throws Exception {
		testInconsistentTripleCO(new API_CompanyToITTriplesForTesting(builder).getModel_MissingCEO(), 0, 0);
	}

	@Test
	public void testWrongNames_CO() throws Exception {
		testInconsistentTripleCO(new API_CompanyToITTriplesForTesting(builder).getModel_WrongNames(), 0, 0);
	}

	@Test
	public void testConsistentTriple_CO() throws Exception {
		testConsistentTripleCO(new API_CompanyToITTriplesForTesting(builder).getModel_ConsistentTriple(), 10);
	}

	@Test
	public void testConsistentTripleWithEmployee_CO() throws Exception {
		testConsistentTripleCO(new API_CompanyToITTriplesForTesting(builder).getModel_ConsistentTripleWithEmployees(), 0);
	}

	@Test
	public void testExtraCEO_CO() throws Exception {
		testInconsistentTripleCO(new API_CompanyToITTriplesForTesting(builder).getModel_ExtraCEO(), 0, 0);
	}

	@Test
	public void testMissingCEO_CC() throws Exception {
		testInconsistentTripleCC(new API_CompanyToITTriplesForTesting(builder).getModel_MissingCEO(), 0, 0);
	}

	@Test
	public void testWrongNames_CC() throws Exception {
		testInconsistentTripleCC(new API_CompanyToITTriplesForTesting(builder).getModel_WrongNames(), 0, 0);
	}

	@Test
	public void testConsistentTriple_CC() throws Exception {
		testConsistentTripleCC(new API_CompanyToITTriplesForTesting(builder).getModel_ConsistentTriple(), 0);
	}

	@Test
	public void testConsistentTripleWithEmployee_CC() throws Exception {
		testConsistentTripleCC(new API_CompanyToITTriplesForTesting(builder).getModel_ConsistentTripleWithEmployees(), 0);
	}

	@Test
	public void testExtraCEO_CC() throws Exception {
		testInconsistentTripleCC(new API_CompanyToITTriplesForTesting(builder).getModel_ExtraCEO(), 0, 0);
	}

}
