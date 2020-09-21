package org.emoflon.neo.example.companytoit;

import org.emoflon.neo.api.org.emoflon.neo.example.companytoit.API_CompanyToITTriplesForTesting;
import org.emoflon.neo.example.ENeoTest;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import CompanyToIT.run.CompanyToIT_CC_Run;
import CompanyToIT.run.CompanyToIT_CO_Run;

public class DirectCOAndCCTests extends ENeoTest {

	private API_CompanyToITTriplesForTesting api = new API_CompanyToITTriplesForTesting(builder);

	@Test
	public void testConsistentTriple1_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc1(), //
				api.getModel_ConsistentTrg1(), //
				api.getRule_CreateCorrs1().rule());
		testForConsistency(new CompanyToIT_CO_Run("ConsistentSrc1", "ConsistentTrg1").runCheckOnly(), 12);
	}

	@Test
	public void testConsistentTriple1_CC() throws Exception {
		exportTriple(api.getModel_ConsistentSrc1(), //
				api.getModel_ConsistentTrg1());
		testForConsistency(new CompanyToIT_CC_Run("ConsistentSrc1", "ConsistentTrg1").runCorrCreation(), 12);
	}

	@Test
	public void testInconsistentTriple1_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc1(), //
				api.getModel_ConsistentTrg1());
		testForInconsistency(new CompanyToIT_CO_Run("ConsistentSrc1", "ConsistentTrg1").runCheckOnly(), 4, 7);
	}

	// ---

	@Test
	public void testConsistentTriple2_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc2(), //
				api.getModel_ConsistentTrg2(), //
				api.getRule_CreateCorrs2().rule());
		testForConsistency(new CompanyToIT_CO_Run("ConsistentSrc2", "ConsistentTrg2").runCheckOnly(), 20);
	}

	@Test
	public void testConsistentTriple2_CC() throws Exception {
		exportTriple(api.getModel_ConsistentSrc2(), //
				api.getModel_ConsistentTrg2());
		testForConsistency(new CompanyToIT_CC_Run("ConsistentSrc2", "ConsistentTrg2").runCorrCreation(), 20);
	}

	@Test
	public void testInconsistentTriple2_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc2(), //
				api.getModel_ConsistentTrg2());
		testForInconsistency(new CompanyToIT_CO_Run("ConsistentSrc2", "ConsistentTrg2").runCheckOnly(), 4, 14);
	}

	// ---

	@Test
	public void testConsistentTriple3_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc3(), //
				api.getModel_ConsistentTrg3(), //
				api.getRule_CreateCorrs3().rule());
		testForConsistency(new CompanyToIT_CO_Run("ConsistentSrc3", "ConsistentTrg3").runCheckOnly(), 24);
	}

	@Test
	public void testConsistentTriple3_CC() throws Exception {
		exportTriple(api.getModel_ConsistentSrc3(), //
				api.getModel_ConsistentTrg3());
		testForConsistency(new CompanyToIT_CC_Run("ConsistentSrc3", "ConsistentTrg3").runCorrCreation(), 24);
	}

	@Test
	public void testInconsistentTriple3_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc3(), //
				api.getModel_ConsistentTrg3());
		testForInconsistency(new CompanyToIT_CO_Run("ConsistentSrc3", "ConsistentTrg3").runCheckOnly(), 4, 18);
	}

	// ---

	@Test
	public void testConsistentTriple4_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc4(), //
				api.getModel_ConsistentTrg4(), //
				api.getRule_CreateCorrs4().rule());
		testForConsistency(new CompanyToIT_CO_Run("ConsistentSrc4", "ConsistentTrg4").runCheckOnly(), 32);
	}

	@Ignore("Non-deterministic")
	public void testConsistentTriple4_CC() throws Exception {
		exportTriple(api.getModel_ConsistentSrc4(), //
				api.getModel_ConsistentTrg4());
		testForConsistency(new CompanyToIT_CC_Run("ConsistentSrc4", "ConsistentTrg4").runCorrCreation(), 32);
	}

	@Test
	public void testInconsistentTriple4_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc4(), //
				api.getModel_ConsistentTrg4());
		testForInconsistency(new CompanyToIT_CO_Run("ConsistentSrc4", "ConsistentTrg4").runCheckOnly(), 4, 25);
	}
}
