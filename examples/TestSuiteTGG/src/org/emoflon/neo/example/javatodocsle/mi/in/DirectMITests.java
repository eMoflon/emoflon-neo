package org.emoflon.neo.example.javatodocsle.mi.in;

import org.emoflon.neo.api.org.emoflon.neo.example.javatodocsle.mi.in.API_DCC;

import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;

import JavaToDocSLE.run.JavaToDocSLE_CO_Run;
import JavaToDocSLE.run.JavaToDocSLE_MI_Run;

public class DirectMITests extends ENeoTest {

	private API_DCC api = new API_DCC(builder);
	
	@Test
	public void testDCC_Chain() throws Exception {
		exportTriple(api.getModel_MoflonJavaChain(), //
				api.getModel_MoflonDocChain(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDocSLE_MI_Run("moflonJavaChain", "moflonDocChain").run();
		testForConsistency(new JavaToDocSLE_CO_Run("moflonJavaChain", "moflonDocChain").runCheckOnly(), 84);
	}
}
