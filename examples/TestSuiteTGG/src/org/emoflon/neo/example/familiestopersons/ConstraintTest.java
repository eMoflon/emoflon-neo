package org.emoflon.neo.example.familiestopersons;

import org.emoflon.neo.api.testsuitetgg.org.emoflon.neo.example.familiestopersons.API_F2PModelForTesting;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;

import org.emoflon.neo.api.familiestopersons.run.FamiliesToPersons_CC_Run;
import org.emoflon.neo.api.familiestopersons.run.FamiliesToPersons_CO_Run;

public class ConstraintTest extends ENeoTest{
	
	private API_F2PModelForTesting api = new API_F2PModelForTesting(builder);
	
	    //co-cc for consistent triple1
		@Test
		public void testConsistentTriple1_CO() throws Exception {
			exportTriple(api.getModel_ConsistentSource1(), //
					api.getModel_ConsistentTarget1(), //
					api.getRule_ConsistentTriple1().rule());
			testForConsistency(new FamiliesToPersons_CO_Run("ConsistentSource1", "ConsistentTarget1").runCheckOnly(), 4);
		}
		
		@Test
		public void testConsistentTriple1_CC() throws Exception {
			exportTriple(api.getModel_ConsistentSource1(), //
					api.getModel_ConsistentTarget1());
			testForConsistency(new FamiliesToPersons_CC_Run("ConsistentSource1", "ConsistentTarget1").runCorrCreation(),
					4);
		}
		
		//co for inconsistent triple1
		@Test
		public void testInConsistentTriple1_CO() throws Exception {
			exportTriple(api.getModel_InConsistentSource1(), //
					api.getModel_InConsistentTarget1(), //
					api.getRule_InConsistentTriple1().rule());
			testForInconsistency(new FamiliesToPersons_CO_Run("InConsistentSource1", "InConsistentTarget1").runCheckOnly(),
					4, 17);
		}
		
		//co-cc for consistent triple2
				@Test
				public void testConsistentTriple2_CO() throws Exception {
					exportTriple(api.getModel_ConsistentSource2(), //
							api.getModel_ConsistentTarget2(), //
							api.getRule_ConsistentTriple2().rule());
					testForConsistency(new FamiliesToPersons_CO_Run("ConsistentSource2", "ConsistentTarget2").runCheckOnly(), 4);
				}
						
				@Test
				public void testConsistentTriple2_CC() throws Exception {
					exportTriple(api.getModel_ConsistentSource2(), //
							api.getModel_ConsistentTarget2());
					testForConsistency(new FamiliesToPersons_CC_Run("ConsistentSource2", "ConsistentTarget2").runCorrCreation(),
							4);
				}
		
		//co for inconsistent triple2
		@Test
		public void testInConsistentTriple2_CO() throws Exception {
			exportTriple(api.getModel_InConsistentSource2(), //
					api.getModel_InConsistentTarget2(), //
					api.getRule_InConsistentTriple2().rule());
			testForInconsistency(new FamiliesToPersons_CO_Run("InConsistentSource2", "InConsistentTarget2").runCheckOnly(),
					4, 17);
		}
				
		
				
		//co for inconsistent triple3
		@Test
		public void testInConsistentTriple3_CO() throws Exception {
			exportTriple(api.getModel_InConsistentSource3(), //
					api.getModel_InConsistentTarget3(), //
					api.getRule_InConsistentTriple3().rule());
			testForInconsistency(new FamiliesToPersons_CO_Run("InConsistentSource3", "InConsistentTarget3").runCheckOnly(),
					4, 19);
		}

}
