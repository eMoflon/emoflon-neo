package org.emoflon.neo.example.companytoit;

import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;

public class DirectCOAndCCTests extends ENeoTest {

	@Test
	public void testConsistentTriple_CO() throws Exception {
		testConsistentTripleCO("ConsistentSrc", "ConsistentTrg", 10);
	}
	
}
