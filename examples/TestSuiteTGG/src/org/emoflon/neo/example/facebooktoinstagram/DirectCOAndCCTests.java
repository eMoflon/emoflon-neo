package org.emoflon.neo.example.facebooktoinstagram;

import org.emoflon.neo.api.facebooktoinstagramfase.run.FacebookToInstagramFASE_CC_Run;
import org.emoflon.neo.api.facebooktoinstagramfase.run.FacebookToInstagramFASE_CO_Run;
import org.emoflon.neo.api.testsuitetgg.org.emoflon.neo.example.facebooktoinstagram.API_FacebookToInstagramTriplesForTesting;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;


public class DirectCOAndCCTests extends ENeoTest {

	private API_FacebookToInstagramTriplesForTesting api = new API_FacebookToInstagramTriplesForTesting(builder);

	@Test
	public void testConsistentTriple1_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc1(), //
				api.getModel_ConsistentTrg1(), //
				api.getRule_CreateCorrs1().rule());
		testForConsistency(new FacebookToInstagramFASE_CO_Run("ConsistentSrc1", "ConsistentTrg1", solver).runCheckOnly(), 3);
	}

	@Test
	public void testConsistentTriple1_CC() throws Exception {
		exportTriple(api.getModel_ConsistentSrc1(), //
				api.getModel_ConsistentTrg1());
		testForConsistency(new FacebookToInstagramFASE_CC_Run("ConsistentSrc1", "ConsistentTrg1", solver).runCorrCreation(),
				3);
	}

	@Test
	public void testInconsistentTriple1_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc1(), //
				api.getModel_ConsistentTrg1());
		testForInconsistency(new FacebookToInstagramFASE_CO_Run("ConsistentSrc1", "ConsistentTrg1", solver).runCheckOnly(),
				0, 2);
	}

	// ---

	@Test
	public void testConsistentTriple2_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc2(), //
				api.getModel_ConsistentTrg2(), //
				api.getRule_CreateCorrs2().rule());
		testForConsistency(new FacebookToInstagramFASE_CO_Run("ConsistentSrc2", "ConsistentTrg2", solver).runCheckOnly(),
				8);
	}

	@Test
	public void testConsistentTriple2_CC() throws Exception {
		exportTriple(api.getModel_ConsistentSrc2(), //
				api.getModel_ConsistentTrg2());
		testForConsistency(new FacebookToInstagramFASE_CC_Run("ConsistentSrc2", "ConsistentTrg2", solver).runCorrCreation(),
				8);
	}

	@Test
	public void testInconsistentTriple2_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc2(), //
				api.getModel_ConsistentTrg2());
		testForInconsistency(new FacebookToInstagramFASE_CO_Run("ConsistentSrc2", "ConsistentTrg2", solver).runCheckOnly(),
				0, 6);
	}
}
