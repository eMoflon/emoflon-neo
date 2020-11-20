package org.emoflon.neo.example.companytoit;

import org.emoflon.neo.api.org.emoflon.neo.example.companytoit.API_C2IModelForTesting;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;

import CompanyToIT.run.CompanyToIT_CC_Run;
import CompanyToIT.run.CompanyToIT_CO_Run;

public class ConstraintTesting extends ENeoTest{

	private API_C2IModelForTesting api = new API_C2IModelForTesting(builder);

	//co-cc for consistent triple1
	@Test
	public void testConsistentTriple1_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSource1(), //
				api.getModel_ConsistentTarget1(), //
				api.getRule_ConsistentTriple1().rule());
		testForConsistency(new CompanyToIT_CO_Run("ConsistentSource1", "ConsistentTarget1").runCheckOnly(), 24);
	}
		
	@Test
	public void testConsistentTriple1_CC() throws Exception {
		exportTriple(api.getModel_ConsistentSource1(), //
				api.getModel_ConsistentTarget1());
		testForConsistency(new CompanyToIT_CC_Run("ConsistentSource1", "ConsistentTarget1").runCorrCreation(),
				24);
	}
		
	//co for inconsistent triple1
	@Test
	public void testInConsistentTriple1_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSource1(), //
				api.getModel_InConsistentTarget1(), //
				api.getRule_InConsistentTriple1().rule());
		testForInconsistency(new CompanyToIT_CO_Run("ConsistentSource1", "InConsistentTarget1").runCheckOnly(),
				24, 4);
	}
			
	//co for inconsistent triple2
	@Test
	public void testInConsistentTriple2_CO() throws Exception {
		exportTriple(api.getModel_InConsistentSource1(), //
				api.getModel_ConsistentTarget1(), //
				api.getRule_InConsistentTriple1().rule());
		testForInconsistency(new CompanyToIT_CO_Run("InConsistentSource1", "ConsistentTarget1").runCheckOnly(),
				24, 4);
	}
	
	//co-cc for consistent triple2
	@Test
	public void testConsistentTriple2_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSource2(), //
				api.getModel_ConsistentTarget2(), //
				api.getRule_ConsistentTriple2().rule());
		testForConsistency(new CompanyToIT_CO_Run("ConsistentSource2", "ConsistentTarget2").runCheckOnly(), 17);
	}
		
	@Test
	public void testConsistentTriple2_CC() throws Exception {
		exportTriple(api.getModel_ConsistentSource2(), //
				api.getModel_ConsistentTarget2());
		testForConsistency(new CompanyToIT_CC_Run("ConsistentSource2", "ConsistentTarget2").runCorrCreation(),
				17);
	}
			
	//co for inconsistent triple3
	@Test
	public void testInConsistentTriple3_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSource2(), //
				api.getModel_InConsistentTarget2(), //
				api.getRule_ConsistentTriple2().rule());
		testForInconsistency(new CompanyToIT_CO_Run("ConsistentSource2", "InConsistentTarget2").runCheckOnly(),
				17, 3);
	}
		
	//co-cc for consistent triple3
		@Test
		public void testConsistentTriple3_CO() throws Exception {
			exportTriple(api.getModel_ConsistentSource3(), //
					api.getModel_ConsistentTarget3(), //
					api.getRule_ConsistentTriple2().rule());
			testForConsistency(new CompanyToIT_CO_Run("ConsistentSource3", "ConsistentTarget3").runCheckOnly(), 17);
		}
			
		@Test
		public void testConsistentTriple3_CC() throws Exception {
			exportTriple(api.getModel_ConsistentSource3(), //
					api.getModel_ConsistentTarget3());
			testForConsistency(new CompanyToIT_CC_Run("ConsistentSource3", "ConsistentTarget3").runCorrCreation(),
					17);
		}
			
		//co for inconsistent triple4
		@Test
		public void testInConsistentTriple4_CO() throws Exception {
			exportTriple(api.getModel_InConsistentSource3(), //
					api.getModel_ConsistentTarget3(), //
					api.getRule_ConsistentTriple2().rule());
			testForInconsistency(new CompanyToIT_CO_Run("InConsistentSource3", "ConsistentTarget3").runCheckOnly(),
					17, 3);
		}	
}
