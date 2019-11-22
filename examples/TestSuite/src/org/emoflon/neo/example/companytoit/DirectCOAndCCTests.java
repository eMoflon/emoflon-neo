package org.emoflon.neo.example.companytoit;

import org.emoflon.neo.api.org.emoflon.neo.example.companytoit.API_CompanyToITTriplesForTesting;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Waiting for fixes")
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
}
