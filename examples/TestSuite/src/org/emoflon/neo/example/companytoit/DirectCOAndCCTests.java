package org.emoflon.neo.example.companytoit;

import org.emoflon.neo.api.org.emoflon.neo.example.companytoit.API_CompanyToITTriplesForTesting;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class DirectCOAndCCTests extends ENeoTest {

	private API_CompanyToITTriplesForTesting api = new API_CompanyToITTriplesForTesting(builder);

	@Test
	public void testConsistentTriple1_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc1(), //
				api.getModel_ConsistentTrg1(), //
				api.getRule_CreateCorrs1().rule());
		testConsistentTripleCO("ConsistentSrc1", "ConsistentTrg1", 12);
	}

	@Test
	public void testConsistentTriple1_CC() throws Exception {
		exportTriple(api.getModel_ConsistentSrc1(), //
				api.getModel_ConsistentTrg1());
		testConsistentTripleCC("ConsistentSrc1", "ConsistentTrg1", 12);
	}

	@Test
	public void testInconsistentTriple1_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc1(), //
				api.getModel_ConsistentTrg1());
		testInconsistentTripleCO("ConsistentSrc1", "ConsistentTrg1", 4, 7);
	}

	// ---

	@Test
	public void testConsistentTriple2_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc2(), //
				api.getModel_ConsistentTrg2(), //
				api.getRule_CreateCorrs2().rule());
		testConsistentTripleCO("ConsistentSrc2", "ConsistentTrg2", 20);
	}

	@Test
	public void testConsistentTriple2_CC() throws Exception {
		exportTriple(api.getModel_ConsistentSrc2(), //
				api.getModel_ConsistentTrg2());
		testConsistentTripleCC("ConsistentSrc2", "ConsistentTrg2", 20);
	}

	@Test
	public void testInconsistentTriple2_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc2(), //
				api.getModel_ConsistentTrg2());
		testInconsistentTripleCO("ConsistentSrc2", "ConsistentTrg2", 4, 14);
	}

	// ---

	@Test
	public void testConsistentTriple3_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc3(), //
				api.getModel_ConsistentTrg3(), //
				api.getRule_CreateCorrs3().rule());
		testConsistentTripleCO("ConsistentSrc3", "ConsistentTrg3", 24);
	}

	@Test
	public void testConsistentTriple3_CC() throws Exception {
		exportTriple(api.getModel_ConsistentSrc3(), //
				api.getModel_ConsistentTrg3());
		testConsistentTripleCC("ConsistentSrc3", "ConsistentTrg3", 24);
	}

	@Test
	public void testInconsistentTriple3_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc3(), //
				api.getModel_ConsistentTrg3());
		testInconsistentTripleCO("ConsistentSrc3", "ConsistentTrg3", 4, 18);
	}

	// ---

	@Test
	public void testConsistentTriple4_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc4(), //
				api.getModel_ConsistentTrg4(), //
				api.getRule_CreateCorrs4().rule());
		testConsistentTripleCO("ConsistentSrc4", "ConsistentTrg4", 32);
	}

	@Disabled("Waiting for a fix")
	@Test
	public void testConsistentTriple4_CC() throws Exception {
		exportTriple(api.getModel_ConsistentSrc4(), //
				api.getModel_ConsistentTrg4());
		testConsistentTripleCC("ConsistentSrc4", "ConsistentTrg4", 32);
	}

	@Test
	public void testInconsistentTriple4_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc4(), //
				api.getModel_ConsistentTrg4());
		testInconsistentTripleCO("ConsistentSrc4", "ConsistentTrg4", 4, 25);
	}
}
