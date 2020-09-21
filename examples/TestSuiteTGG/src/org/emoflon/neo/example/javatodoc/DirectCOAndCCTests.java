package org.emoflon.neo.example.javatodoc;

import org.emoflon.neo.api.org.emoflon.neo.example.javatodoc.API_JavaToDocTriplesForTesting;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;

import JavaToDoc.run.JavaToDoc_CC_Run;
import JavaToDoc.run.JavaToDoc_CO_Run;

public class DirectCOAndCCTests extends ENeoTest {

	private API_JavaToDocTriplesForTesting api = new API_JavaToDocTriplesForTesting(builder);

	@Test
	public void testConsistentTriple1_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc1(), //
				api.getModel_ConsistentTrg1(), //
				api.getRule_CreateCorrs1().rule());
		testForConsistency(new JavaToDoc_CO_Run("ConsistentSrc1", "ConsistentTrg1").runCheckOnly(), 19);
	}

	@Test
	public void testConsistentTriple1_CC() throws Exception {
		exportTriple(api.getModel_ConsistentSrc1(), //
				api.getModel_ConsistentTrg1());
		testForConsistency(new JavaToDoc_CC_Run("ConsistentSrc1", "ConsistentTrg1").runCorrCreation(), 19);
	}

	@Test
	public void testInconsistentTriple1_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc1(), //
				api.getModel_ConsistentTrg1());
		testForInconsistency(new JavaToDoc_CO_Run("ConsistentSrc1", "ConsistentTrg1").runCheckOnly(), 4, 7);
	}

	// ---

	@Test
	public void testConsistentTriple2_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc2(), //
				api.getModel_ConsistentTrg2(), //
				api.getRule_CreateCorrs2().rule());
		testForConsistency(new JavaToDoc_CO_Run("ConsistentSrc2", "ConsistentTrg2").runCheckOnly(), 29);
	}

	@Test
	public void testConsistentTriple2_CC() throws Exception {
		exportTriple(api.getModel_ConsistentSrc2(), //
				api.getModel_ConsistentTrg2());
		testForConsistency(new JavaToDoc_CC_Run("ConsistentSrc2", "ConsistentTrg2").runCorrCreation(), 29);
	}

	@Test
	public void testInconsistentTriple2_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc2(), //
				api.getModel_ConsistentTrg2());
		testForInconsistency(new JavaToDoc_CO_Run("ConsistentSrc2", "ConsistentTrg2").runCheckOnly(), 4, 14);
	}

	// ---

	@Test
	public void testConsistentTriple3_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc3(), //
				api.getModel_ConsistentTrg3(), //
				api.getRule_CreateCorrs3().rule());
		testForConsistency(new JavaToDoc_CO_Run("ConsistentSrc3", "ConsistentTrg3").runCheckOnly(), 39);
	}

	@Test
	public void testConsistentTriple3_CC() throws Exception {
		exportTriple(api.getModel_ConsistentSrc3(), //
				api.getModel_ConsistentTrg3());
		testForConsistency(new JavaToDoc_CC_Run("ConsistentSrc3", "ConsistentTrg3").runCorrCreation(), 39);
	}

	@Test
	public void testInconsistentTriple3_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc3(), //
				api.getModel_ConsistentTrg3());
		testForInconsistency(new JavaToDoc_CO_Run("ConsistentSrc3", "ConsistentTrg3").runCheckOnly(), 4, 18);
	}

	// ---

	@Test
	public void testConsistentTriple4_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc4(), //
				api.getModel_ConsistentTrg4(), //
				api.getRule_CreateCorrs4().rule());
		testForConsistency(new JavaToDoc_CO_Run("ConsistentSrc4", "ConsistentTrg4").runCheckOnly(), 46);
	}

	@Test
	public void testConsistentTriple4_CC() throws Exception {
		exportTriple(api.getModel_ConsistentSrc4(), //
				api.getModel_ConsistentTrg4());
		testForConsistency(new JavaToDoc_CC_Run("ConsistentSrc4", "ConsistentTrg4").runCorrCreation(), 46);
	}

	@Test
	public void testInconsistentTriple4_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc4(), //
				api.getModel_ConsistentTrg4());
		testForInconsistency(new JavaToDoc_CO_Run("ConsistentSrc4", "ConsistentTrg4").runCheckOnly(), 4, 25);
	}
}