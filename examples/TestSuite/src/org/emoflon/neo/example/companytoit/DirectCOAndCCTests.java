package org.emoflon.neo.example.companytoit;

import org.emoflon.neo.api.org.emoflon.neo.example.companytoit.API_CompanyToITTriplesForTesting;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Waiting for fixes")
public class DirectCOAndCCTests extends ENeoTest {

	@Test
	public void testConsistentTriple_CO() throws Exception {
		var api = new API_CompanyToITTriplesForTesting(builder);
		builder.exportEMSLEntityToNeo4j(api.getModel_ConsistentSrc());
		builder.exportEMSLEntityToNeo4j(api.getModel_ConsistentTrg());
		api.getRule_CreateCorrsForConsistentSrcTrg().rule().apply();
		testConsistentTripleCO("ConsistentSrc", "ConsistentTrg", 12);
	}
	
}
