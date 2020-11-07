package org.emoflon.neo.example.javatodocsle.mi.in;

import org.emoflon.neo.api.testsuitetgg.org.emoflon.neo.example.javatodocsle.mi.in.API_DCC;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;


public class DirectMITests extends ENeoTest {

	private API_DCC api = new API_DCC(builder);
	
	@Test
	public void testDCC_Chain() throws Exception {
		exportTriple(api.getModel_MoflonJava(), //
				api.getModel_MoflonDoc(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDocSLE_MI_Run("moflonJava", "moflonDoc").run();
		testForConsistency(new JavaToDocSLE_CO_Run("moflonJava", "moflonDoc", solver).runCheckOnly(), 47);
	}
}
