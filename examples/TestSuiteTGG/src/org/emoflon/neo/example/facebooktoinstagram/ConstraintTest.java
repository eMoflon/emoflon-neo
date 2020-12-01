package org.emoflon.neo.example.facebooktoinstagram;

import org.emoflon.neo.api.org.emoflon.neo.example.facebooktoinstagram.API_ModelForTesting;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;

import Transformations.run.FacebookToInstagramFASE_CC_Run;
import Transformations.run.FacebookToInstagramFASE_CO_Run;


public class ConstraintTest extends ENeoTest{

	private API_ModelForTesting api = new API_ModelForTesting(builder);
	
	//co-cc for consistent triple
	@Test
	public void testConsistentTriple_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSource(), //
				api.getModel_ConsistentTarget(), //
				api.getRule_ConsistentTriple().rule());

		testForConsistency(new FacebookToInstagramFASE_CO_Run("ConsistentSource", "ConsistentTarget").runCheckOnly(), 28);
	}
	
	@Test
	public void testConsistentTriple_CC() throws Exception {
		exportTriple(api.getModel_ConsistentSource(), //
				api.getModel_ConsistentTarget());

		testForConsistency(new FacebookToInstagramFASE_CC_Run("ConsistentSorce", "ConsistentTarget").runCorrCreation(),
				28);
	}
	
	//co for inconsistent triple1
	@Test
	public void testInConsistentTriple1_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSource(), //
				api.getModel_InConsistentTarget1(), //
				api.getRule_ConsistentTriple().rule());

		testForInconsistency(new FacebookToInstagramFASE_CO_Run("ConsistentSource", "InConsistentTarget1").runCheckOnly(),
				28, 1);
	}
			
	//co for inconsistent triple2
	@Test
	public void testInConsistentTriple2_CO() throws Exception {
		exportTriple(api.getModel_InConsistentSource1(), //
				api.getModel_ConsistentTarget(), //
				api.getRule_ConsistentTriple().rule());

		testForInconsistency(new FacebookToInstagramFASE_CO_Run("InConsistentSource1", "ConsistentTarget").runCheckOnly(),
				28, 5);
	}
	
	//co for inconsistent triple3
	@Test
	public void testInConsistentTriple3_CO() throws Exception {
		exportTriple(api.getModel_InConsistentSource2(), //
				api.getModel_ConsistentTarget(), //
				api.getRule_ConsistentTriple().rule());

		testForInconsistency(new FacebookToInstagramFASE_CO_Run("InConsistentSource2", "ConsistentTarget").runCheckOnly(),
				28, 5);
	}
	
}
