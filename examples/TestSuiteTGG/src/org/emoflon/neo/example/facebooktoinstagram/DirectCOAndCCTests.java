package org.emoflon.neo.example.facebooktoinstagram;

import org.emoflon.neo.api.org.emoflon.neo.example.facebooktoinstagram.API_FacebookToInstagramTriplesForTesting;
import org.emoflon.neo.example.ENeoTest;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import Transformations.run.FacebookToInstagramFASE_CC_Run;
import Transformations.run.FacebookToInstagramFASE_CO_Run;

public class DirectCOAndCCTests extends ENeoTest {

	private API_FacebookToInstagramTriplesForTesting api = new API_FacebookToInstagramTriplesForTesting(builder);

	@Test
	public void testConsistentTriple1_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc1(), //
				api.getModel_ConsistentTrg1(), //
				api.getRule_CreateCorrs1().rule());
		testForConsistency(new FacebookToInstagramFASE_CO_Run("ConsistentSrc1", "ConsistentTrg1", solver).runCheckOnly(), 9);
	}

	@Test
	public void testConsistentTriple1_CC() throws Exception {
		exportTriple(api.getModel_ConsistentSrc1(), //
				api.getModel_ConsistentTrg1());
		testForConsistency(new FacebookToInstagramFASE_CC_Run("ConsistentSrc1", "ConsistentTrg1", solver).runCorrCreation(),
				9);
	}

	@Test
	public void testInconsistentTriple1_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc1(), //
				api.getModel_ConsistentTrg1(),
				api.getRule_CreateCorrs1().rule());
		testForInconsistency(new FacebookToInstagramFASE_CO_Run("ConsistentSrc1", "ConsistentTrg1", solver).runCheckOnly(),
				4, 4);
	}

	// ---

	@Test
	public void testConsistentTriple2_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc2(), //
				api.getModel_ConsistentTrg2(), //
				api.getRule_CreateCorrs2().rule());
		testForConsistency(new FacebookToInstagramFASE_CO_Run("ConsistentSrc2", "ConsistentTrg2", solver).runCheckOnly(),
				16);
	}

	@Test
	public void testConsistentTriple2_CC() throws Exception {
		exportTriple(api.getModel_ConsistentSrc2(), //
				api.getModel_ConsistentTrg2());
		testForConsistency(new FacebookToInstagramFASE_CC_Run("ConsistentSrc2", "ConsistentTrg2", solver).runCorrCreation(),
				16);
	}

	@Test
	public void testInconsistentTriple2_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc2(), //
				api.getModel_ConsistentTrg2(),
				api.getRule_CreateCorrs2().rule());
		testForInconsistency(new FacebookToInstagramFASE_CO_Run("ConsistentSrc2", "ConsistentTrg2", solver).runCheckOnly(),
				4, 10);
	}

	// ---

	@Ignore("TGG is not suitable for OPT strategies in this form")
	public void testConsistentTriple3_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc3(), //
				api.getModel_ConsistentTrg3(), //
				api.getRule_CreateCorrs3().rule());
		testForConsistency(new FacebookToInstagramFASE_CO_Run("ConsistentSrc3", "ConsistentTrg3", solver).runCheckOnly(),
				30);
	}

	@Ignore("TGG is not suitable for OPT strategies in this form")
	public void testConsistentTriple3_CC() throws Exception {
		exportTriple(api.getModel_ConsistentSrc3(), //
				api.getModel_ConsistentTrg3());
		testForConsistency(new FacebookToInstagramFASE_CC_Run("ConsistentSrc3", "ConsistentTrg3", solver).runCorrCreation(),
				30);
	}

	@Ignore("TGG is not suitable for OPT strategies in this form")
	public void testInconsistentTriple3_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSrc3(), //
				api.getModel_ConsistentTrg3(),
				api.getRule_CreateCorrs3().rule());
		testForInconsistency(new FacebookToInstagramFASE_CO_Run("ConsistentSrc3", "ConsistentTrg3", solver).runCheckOnly(),
				6, 21);
	}
}
