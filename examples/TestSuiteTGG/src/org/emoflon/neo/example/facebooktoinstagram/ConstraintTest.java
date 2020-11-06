package org.emoflon.neo.example.facebooktoinstagram;

import org.emoflon.neo.api.org.emoflon.neo.example.facebooktoinstagram.API_ModelForTesting;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;

import Transformations.run.FacebookToInstagramGrammar_CC_Run;
import Transformations.run.FacebookToInstagramGrammar_CO_Run;


public class ConstraintTest extends ENeoTest{

	private API_ModelForTesting api = new API_ModelForTesting(builder);
	
	//co-cc for consistent triple
	@Test
	public void testConsistentTriple_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSorce(), //
				api.getModel_ConsistentTarget(), //
				api.getRule_ConsistentTriple().rule());
		testForConsistency(new FacebookToInstagramGrammar_CO_Run("ConsistentSorce", "ConsistentTarget").runCheckOnly(), 28);
	}
	
	@Test
	public void testConsistentTriple_CC() throws Exception {
		exportTriple(api.getModel_ConsistentSorce(), //
				api.getModel_ConsistentTarget());
		testForConsistency(new FacebookToInstagramGrammar_CC_Run("ConsistentSorce", "ConsistentTarget").runCorrCreation(),
				28);
	}
	
	//co for inconsistent triple1
	@Test
	public void testInConsistentTriple1_CO1() throws Exception {
		exportTriple(api.getModel_ConsistentSorce(), //
				api.getModel_ConsistentTarget(), //
				api.getRule_InConsistentTriple1().rule());
		testForInconsistency(new FacebookToInstagramGrammar_CO_Run("ConsistentSorce", "ConsistentTarget").runCheckOnly(),
				28, 27);
	}
			
	//co for inconsistent triple2
	@Test
	public void testInConsistentTriple2_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSorce(), //
				api.getModel_ConsistentTarget(), //
				api.getRule_InConsistentTriple2().rule());
		testForInconsistency(new FacebookToInstagramGrammar_CO_Run("ConsistentSorce", "ConsistentTarget").runCheckOnly(),
				28, 23);
	}
	
	//co for inconsistent triple3
	@Test
	public void testInConsistentTriple3_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSorce(), //
				api.getModel_ConsistentTarget(), //
				api.getRule_InConsistentTriple3().rule());
		testForInconsistency(new FacebookToInstagramGrammar_CO_Run("ConsistentSorce", "ConsistentTarget").runCheckOnly(),
				28, 33);
	}
	
}
