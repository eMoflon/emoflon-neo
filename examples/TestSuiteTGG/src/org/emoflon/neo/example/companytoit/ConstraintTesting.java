package org.emoflon.neo.example.companytoit;

import org.emoflon.neo.api.org.emoflon.neo.example.companytoit.API_C2IModelForTesting;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;

import CompanyToIT.run.CompanyToIT_CO_Run;

public class ConstraintTesting extends ENeoTest{

	private API_C2IModelForTesting api = new API_C2IModelForTesting(builder);

	//co-cc for consistent triple1
	@Test
	public void testConsistentTriple1_CO() throws Exception {
		exportTriple(api.getModel_ConsistentSource1(), //
				api.getModel_ConsistentTarget1(), //
				api.getRule_ConsistentTriple1().rule());
		testForConsistency(new CompanyToIT_CO_Run("ConsistentSource1", "ConsistentTarget1").runCheckOnly(), 40);
	}
		
//	@Test
//	public void testConsistentTriple1_CC() throws Exception {
//		exportTriple(api.getModel_ConsistentSource1(), //
//				api.getModel_ConsistentTarget1());
//		testForConsistency(new CompanyToIT_CC_Run("ConsistentSource1", "ConsistentTarget1").runCorrCreation(),
//				40);
//	}
		
	//co for inconsistent triple1
	@Test
	public void testInconsistentTriple1_CO() throws Exception {
		exportTriple(api.getModel_InconsistentSource1(), //
				api.getModel_InconsistentTarget1(), //
				api.getRule_InconsistentTriple1().rule());
		testForInconsistency(new CompanyToIT_CO_Run("InconsistentSource1", "InconsistentTarget1").runCheckOnly(),
				12, 20);
	}
	
//	@Test
//	public void testInconsistentTriple1_CC() throws Exception {
//		exportTriple(api.getModel_InconsistentSource1(), //
//				api.getModel_InconsistentTarget1());
//		testForInconsistency(new CompanyToIT_CC_Run("InconsistentSource1", "InconsistentTarget1").runCorrCreation(),
//				4, 12);
//	}
	
	//co for inconsistent triple2
	@Test
	public void testInconsistentTriple2_CO() throws Exception {
		exportTriple(api.getModel_InconsistentSource2(), //
				api.getModel_InconsistentTarget2(), //
				api.getRule_InconsistentTriple2().rule());
		testForInconsistency(new CompanyToIT_CO_Run("InconsistentSource2", "InconsistentTarget2").runCheckOnly(),
				12, 25);
	}
	
//	@Test
//	public void testInconsistentTriple2_CC() throws Exception {
//		exportTriple(api.getModel_InconsistentSource2(), //
//				api.getModel_InconsistentTarget2());
//		testForInconsistency(new CompanyToIT_CC_Run("InconsistentSource2", "InconsistentTarget2").runCorrCreation(),
//				4, 12);
//	}
	
	//co for inconsistent triple3
	@Test
	public void testInconsistentTriple3_CO() throws Exception {
		exportTriple(api.getModel_InconsistentSource3(), //
				api.getModel_InconsistentTarget3(), //
				api.getRule_InconsistentTriple3().rule());
		testForInconsistency(new CompanyToIT_CO_Run("InconsistentSource3", "InconsistentTarget3").runCheckOnly(),
				12, 25);
	}
	
//	@Test
//	public void testInconsistentTriple3_CC() throws Exception {
//		exportTriple(api.getModel_InconsistentSource3(), //
//				api.getModel_InconsistentTarget3());
//		testForInconsistency(new CompanyToIT_CC_Run("InconsistentSource3", "InconsistentTarget3").runCorrCreation(),
//				4, 12);
//	}
	
}
