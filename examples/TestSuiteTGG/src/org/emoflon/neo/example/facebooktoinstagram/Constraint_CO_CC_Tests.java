package org.emoflon.neo.example.facebooktoinstagram;

import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;

import org.emoflon.neo.api.facebooktoinstagram_constrained.run.FacebookToInstagram_Constrained_CC_Run;
import org.emoflon.neo.api.facebooktoinstagram_constrained.run.FacebookToInstagram_Constrained_CO_Run;
import org.emoflon.neo.api.testsuitetgg.org.emoflon.neo.example.facebooktoinstagram.API_FacebookToInstagramTriplesForConstraintTesting;


public class Constraint_CO_CC_Tests extends ENeoTest{

	private API_FacebookToInstagramTriplesForConstraintTesting api = new API_FacebookToInstagramTriplesForConstraintTesting(builder);
	
	//co-cc for consistent triple
	@Test
	public void testConsistentTriple_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSource(), //
				api.getModel_ConsistentTarget(), //
				api.getRule_ConsistentTriple().rule());

		testForConsistency(new FacebookToInstagram_Constrained_CO_Run("ConsistentSource", "ConsistentTarget").runCheckOnly(), 19);
	}
	
	
	@Test
	public void testConsistentTriple_CC() throws Exception {
		exportTriple(api.getModel_ConsistentSource(), //
				api.getModel_ConsistentTarget());
		testForConsistency(new FacebookToInstagram_Constrained_CC_Run("ConsistentSource", "ConsistentTarget").runCorrCreation(),
				19);
	}
			
	//co for inconsistent triple2
	@Test
	public void testInConsistentTriple2_CO() throws Exception {
		exportTriple(api.getModel_InConsistentSource2(), //
				api.getModel_InConsistentTarget2(), //
				api.getRule_InConsistentTriple2().rule());

		testForInconsistency(new FacebookToInstagram_Constrained_CO_Run("InConsistentSource2", "InConsistentTarget2").runCheckOnly(),
				14, 4);
	}
	
	//cc for inconsistent triple2
	@Test
	public void testInConsistentTriple2_CC() throws Exception {
		exportTriple(api.getModel_InConsistentSource2(), //
				api.getModel_InConsistentTarget2(), //
				api.getRule_InConsistentTriple2().rule());

		testForInconsistency(new FacebookToInstagram_Constrained_CC_Run("InConsistentSource2", "InConsistentTarget2").runCorrCreation(),
				14, 7);
	}
	
	//co for inconsistent triple3
	@Test
	public void testInConsistentTriple3_CO() throws Exception {
		exportTriple(api.getModel_InConsistentSource3(), //
				api.getModel_InConsistentTarget3(), //
				api.getRule_InConsistentTriple3().rule());

		testForInconsistency(new FacebookToInstagram_Constrained_CO_Run("InConsistentSource3", "InConsistentTarget3").runCheckOnly(),
				19, 4);
	}
	
	//cc for inconsistent triple3
	@Test
	public void testInConsistentTriple3_CC() throws Exception {
		exportTriple(api.getModel_InConsistentSource3(), //
				api.getModel_InConsistentTarget3(), //
				api.getRule_InConsistentTriple3().rule());

		testForInconsistency(new FacebookToInstagram_Constrained_CC_Run("InConsistentSource3", "InConsistentTarget3").runCorrCreation(),
				19, 7);
	}
	
}
