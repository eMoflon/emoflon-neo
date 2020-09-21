package org.emoflon.neo.example.javatodoc;

import org.emoflon.neo.api.org.emoflon.neo.example.javatodoc.mi.in.API_DCC;

import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;

import JavaToDoc.run.JavaToDoc_CO_Run;
import JavaToDoc.run.JavaToDoc_MI_Run;

public class DirectMITests extends ENeoTest {

	private API_DCC api = new API_DCC(builder);
	
	@Test
	public void testDCC_Chain() throws Exception {
		exportTriple(api.getModel_MoflonJavaChain(), //
				api.getModel_MoflonDocChain(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDoc_MI_Run("moflonJavaChain", "moflonDocChain").run();
		testForConsistency(new JavaToDoc_CO_Run("moflonJavaChain", "moflonDocChain").runCheckOnly(), 84);
	}

	@Test
	public void testDCC_MultiDel() throws Exception {
		exportTriple(api.getModel_MoflonJavaMultiDel(), //
				api.getModel_MoflonDocMultiDel(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDoc_MI_Run("moflonJavaMultiDel", "moflonDocMultiDel").run();
		testForConsistency(new JavaToDoc_CO_Run("moflonJavaMultiDel", "moflonDocMultiDel").runCheckOnly(), 21);
	}
	
	@Test
	public void testDCC_Simple() throws Exception {
		exportTriple(api.getModel_MoflonJavaSimple(), //
				api.getModel_MoflonDocSimple(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDoc_MI_Run("moflonJavaSimple", "moflonDocSimple").run();
		testForConsistency(new JavaToDoc_CO_Run("moflonJavaSimple", "moflonDocSimple").runCheckOnly(), 84);
	}
}
