package org.emoflon.neo.example.familiestopersons;

import org.emoflon.neo.api.org.emoflon.neo.example.familiestopersons.API_F2PModelForTesting;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;

import Schema.run.FamiliesToPersons_CC_Run;
import Schema.run.FamiliesToPersons_CO_Run;

public class ConstraintTest extends ENeoTest{
	
	private API_F2PModelForTesting api = new API_F2PModelForTesting(builder);
	
	    //co-cc for consistent triple1
		@Test
		public void testConsistentTriple1_CO() throws Exception {
			exportTriple(api.getModel_ConsistentSource1(), //
					api.getModel_ConsistentTarget1(), //
					api.getRule_ConsistentTriple1().rule());
			testForConsistency(new FamiliesToPersons_CO_Run("ConsistentSource1", "ConsistentTarget1").runCheckOnly(), 24);
		}
		
		@Test
		public void testConsistentTriple1_CC() throws Exception {
			exportTriple(api.getModel_ConsistentSource1(), //
					api.getModel_ConsistentTarget1());
			testForConsistency(new FamiliesToPersons_CC_Run("ConsistentSource1", "ConsistentTarget1").runCorrCreation(),
					24);
		}
		
		//co for inconsistent triple1
		@Test
		public void testInConsistentTriple1_CO() throws Exception {
			exportTriple(api.getModel_ConsistentSource1(), //
					api.getModel_ConsistentTarget1(), //
					api.getRule_InConsistentTriple1().rule());
			testForInconsistency(new FamiliesToPersons_CO_Run("ConsistentSource1", "ConsistentTarget1").runCheckOnly(),
					24, 21);
		}
		
		//co for inconsistent triple2
		@Test
		public void testInConsistentTriple2_CO() throws Exception {
			exportTriple(api.getModel_ConsistentSource2(), //
					api.getModel_ConsistentTarget1(), //
					api.getRule_InConsistentTriple2().rule());
			testForInconsistency(new FamiliesToPersons_CO_Run("ConsistentSource1", "ConsistentTarget1").runCheckOnly(),
					24, 20);
		}
				
		//co-cc for consistent triple2
		@Test
		public void testConsistentTriple2_CO() throws Exception {
			exportTriple(api.getModel_ConsistentSource2(), //
					api.getModel_ConsistentTarget2(), //
					api.getRule_ConsistentTriple2().rule());
			testForConsistency(new FamiliesToPersons_CO_Run("ConsistentSource2", "ConsistentTarget2").runCheckOnly(), 24);
		}
				
		@Test
		public void testConsistentTriple2_CC() throws Exception {
			exportTriple(api.getModel_ConsistentSource2(), //
					api.getModel_ConsistentTarget2());
			testForConsistency(new FamiliesToPersons_CC_Run("ConsistentSource2", "ConsistentTarget2").runCorrCreation(),
					24);
		}
				
		//co for inconsistent triple3
		@Test
		public void testInConsistentTriple3_CO() throws Exception {
			exportTriple(api.getModel_ConsistentSource2(), //
					api.getModel_ConsistentTarget2(), //
					api.getRule_InConsistentTriple3().rule());
			testForInconsistency(new FamiliesToPersons_CO_Run("ConsistentSource2", "ConsistentTarget2").runCheckOnly(),
					24, 20);
		}
				
		//co for inconsistent triple4
		@Test
		public void testInConsistentTriple4_CO() throws Exception {
			exportTriple(api.getModel_ConsistentSource2(), //
					api.getModel_ConsistentTarget2(), //
					api.getRule_InConsistentTriple4().rule());
			testForInconsistency(new FamiliesToPersons_CO_Run("ConsistentSource2", "ConsistentTarget2").runCheckOnly(),
					24, 20);
		}
		
		//co-cc for consistent triple3
		@Test
		public void testConsistentTriple3_CO() throws Exception {
			exportTriple(api.getModel_ConsistentSource3(), //
					api.getModel_ConsistentTarget3(), //
					api.getRule_ConsistentTriple3().rule());
			testForConsistency(new FamiliesToPersons_CO_Run("ConsistentSource3", "ConsistentTarget3").runCheckOnly(), 17);
		}
					
		@Test
		public void testConsistentTriple3_CC() throws Exception {
			exportTriple(api.getModel_ConsistentSource3(), //
					api.getModel_ConsistentTarget3());
			testForConsistency(new FamiliesToPersons_CC_Run("ConsistentSource3", "ConsistentTarget3").runCorrCreation(),
					17);
		}
						
		//co for inconsistent triple5
		@Test
		public void testInConsistentTriple5_CO() throws Exception {
			exportTriple(api.getModel_ConsistentSource3(), //
					api.getModel_ConsistentTarget3(), //
					api.getRule_InConsistentTriple5().rule());
			testForInconsistency(new FamiliesToPersons_CO_Run("ConsistentSource3", "ConsistentTarget3").runCheckOnly(),
					17, 24);
		}
		
		//co-cc for consistent triple4
		@Test
		public void testConsistentTriple4_CO() throws Exception {
			exportTriple(api.getModel_ConsistentSource4(), //
					api.getModel_ConsistentTarget4(), //
					api.getRule_ConsistentTriple4().rule());
			testForConsistency(new FamiliesToPersons_CO_Run("ConsistentSource4", "ConsistentTarget4").runCheckOnly(), 17);
		}
						
		@Test
		public void testConsistentTriple4_CC() throws Exception {
			exportTriple(api.getModel_ConsistentSource4(), //
					api.getModel_ConsistentTarget4());
			testForConsistency(new FamiliesToPersons_CC_Run("ConsistentSource4", "ConsistentTarget4").runCorrCreation(),
					17);
		}
								
		//co for inconsistent triple6
		@Test
		public void testInConsistentTriple6_CO() throws Exception {
			exportTriple(api.getModel_ConsistentSource4(), //
					api.getModel_ConsistentTarget4(), //
					api.getRule_InConsistentTriple6().rule());
			testForInconsistency(new FamiliesToPersons_CO_Run("ConsistentSource4", "ConsistentTarget4").runCheckOnly(),
					17, 24);
		}

}
