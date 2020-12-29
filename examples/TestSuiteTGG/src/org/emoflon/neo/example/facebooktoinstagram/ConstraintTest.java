package org.emoflon.neo.example.facebooktoinstagram;

import org.emoflon.neo.api.org.emoflon.neo.example.facebooktoinstagram.API_ModelForTesting;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;

import Schema.run.FamiliesToPersons_CC_Run;
import Transformations.run.FacebookToInstagramFASE_CO_Run;


public class ConstraintTest extends ENeoTest{

	private API_ModelForTesting api = new API_ModelForTesting(builder);
	
	//co-cc for consistent triple
	@Test
	public void testConsistentTriple_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSource(), //
				api.getModel_ConsistentTarget(), //
				api.getRule_ConsistentTriple().rule());

		testForConsistency(new FacebookToInstagramFASE_CO_Run("ConsistentSource", "ConsistentTarget").runCheckOnly(), 9);
	}
	
	
	@Test
	public void testConsistentTriple_CC() throws Exception {
		exportTriple(api.getModel_ConsistentSource(), //
				api.getModel_ConsistentTarget());
		testForConsistency(new FamiliesToPersons_CC_Run("ConsistentSource", "ConsistentTarget").runCorrCreation(),
				9);
	}
	

	//co for inconsistent triple1
	@Test
	public void testInConsistentTriple1_CO() throws Exception {
		exportTriple(api.getModel_InConsistentSource1(), //
				api.getModel_InConsistentTarget1(), //
				api.getRule_InConsistentTriple1().rule());

		testForInconsistency(new FacebookToInstagramFASE_CO_Run("InConsistentSource1", "InConsistentTarget1").runCheckOnly(),
				9, 20);
	}
			
	//co for inconsistent triple2
	@Test
	public void testInConsistentTriple2_CO() throws Exception {
		exportTriple(api.getModel_InConsistentSource2(), //
				api.getModel_InConsistentTarget2(), //
				api.getRule_InConsistentTriple2().rule());

		testForInconsistency(new FacebookToInstagramFASE_CO_Run("InConsistentSource2", "InConsistentTarget2").runCheckOnly(),
				9, 14);
	}
	
	//co for inconsistent triple3
	@Test
	public void testInConsistentTriple3_CO() throws Exception {
		exportTriple(api.getModel_InConsistentSource3(), //
				api.getModel_InConsistentTarget3(), //
				api.getRule_InConsistentTriple3().rule());

		testForInconsistency(new FacebookToInstagramFASE_CO_Run("InConsistentSource3", "InConsistentTarget3").runCheckOnly(),
				9, 24);
	}
	
}
