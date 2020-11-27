package org.emoflon.neo.example.javatodoc;

import org.emoflon.neo.api.javatodoc.run.JavaToDoc_CC_Run;
import org.emoflon.neo.api.javatodoc.run.JavaToDoc_CO_Run;
import org.emoflon.neo.api.testsuitetgg.org.emoflon.neo.example.javatodoc.API_JavaToDocTriplesForTesting;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;

public class DirectCOAndCCTests extends ENeoTest {

	private API_JavaToDocTriplesForTesting api = new API_JavaToDocTriplesForTesting(builder);

	@Test
	public void testConsistentTriple1_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc1(), //
				api.getModel_ConsistentTrg1(), //
				api.getRule_CreateCorrs1().rule());
		testForConsistency(new JavaToDoc_CO_Run("ConsistentSrc1", "ConsistentTrg1", solver).runCheckOnly(), 10);
	}

	@Test
	public void testConsistentTriple1_CC() throws Exception {
		exportTriple(api.getModel_ConsistentSrc1(), //
				api.getModel_ConsistentTrg1());
		testForConsistency(new JavaToDoc_CC_Run("ConsistentSrc1", "ConsistentTrg1", solver).runCorrCreation(), 10);
	}

	@Test
	public void testInconsistentTriple1_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc1(), //
				api.getModel_ConsistentTrg1());
		testForInconsistency(new JavaToDoc_CO_Run("ConsistentSrc1", "ConsistentTrg1", solver).runCheckOnly(), 0, 8);
	}

	// ---

	@Test
	public void testConsistentTriple2_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc2(), //
				api.getModel_ConsistentTrg2(), //
				api.getRule_CreateCorrs2().rule());
		testForConsistency(new JavaToDoc_CO_Run("ConsistentSrc2", "ConsistentTrg2", solver).runCheckOnly(), 17);
	}

	@Test
	public void testConsistentTriple2_CC() throws Exception {
		exportTriple(api.getModel_ConsistentSrc2(), //
				api.getModel_ConsistentTrg2());
		testForConsistency(new JavaToDoc_CC_Run("ConsistentSrc2", "ConsistentTrg2", solver).runCorrCreation(), 17);
	}

	@Test
	public void testInconsistentTriple2_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc2(), //
				api.getModel_ConsistentTrg2());
		testForInconsistency(new JavaToDoc_CO_Run("ConsistentSrc2", "ConsistentTrg2", solver).runCheckOnly(), 0, 14);
	}

	// ---

	@Test
	public void testConsistentTriple3_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc3(), //
				api.getModel_ConsistentTrg3(), //
				api.getRule_CreateCorrs3().rule());
		testForConsistency(new JavaToDoc_CO_Run("ConsistentSrc3", "ConsistentTrg3", solver).runCheckOnly(), 24);
	}

	@Test
	public void testConsistentTriple3_CC() throws Exception {
		exportTriple(api.getModel_ConsistentSrc3(), //
				api.getModel_ConsistentTrg3());
		testForConsistency(new JavaToDoc_CC_Run("ConsistentSrc3", "ConsistentTrg3", solver).runCorrCreation(), 24);
	}

	@Test
	public void testInconsistentTriple3_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc3(), //
				api.getModel_ConsistentTrg3());
		testForInconsistency(new JavaToDoc_CO_Run("ConsistentSrc3", "ConsistentTrg3", solver).runCheckOnly(), 0, 20);
	}

	// ---

	@Test
	public void testConsistentTriple4_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc4(), //
				api.getModel_ConsistentTrg4(), //
				api.getRule_CreateCorrs4().rule());
		testForConsistency(new JavaToDoc_CO_Run("ConsistentSrc4", "ConsistentTrg4", solver).runCheckOnly(), 29);
	}

	@Test
	public void testConsistentTriple4_CC() throws Exception {
		exportTriple(api.getModel_ConsistentSrc4(), //
				api.getModel_ConsistentTrg4());
		testForConsistency(new JavaToDoc_CC_Run("ConsistentSrc4", "ConsistentTrg4", solver).runCorrCreation(), 29);
	}

	@Test
	public void testInconsistentTriple4_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc4(), //
				api.getModel_ConsistentTrg4());
		testForInconsistency(new JavaToDoc_CO_Run("ConsistentSrc4", "ConsistentTrg4", solver).runCheckOnly(), 0, 24);
	}
}
