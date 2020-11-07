package org.emoflon.neo.example.javatodoc;

import org.emoflon.neo.api.javatodoc.run.JavaToDoc_CO_Run;
import org.emoflon.neo.api.javatodoc.run.JavaToDoc_MI_Run;
import org.emoflon.neo.api.testsuitetgg.org.emoflon.neo.example.javatodoc.mi.in.API_DCC;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;

public class DirectMITests extends ENeoTest {

	private API_DCC api = new API_DCC(builder);
	
	@Test
	public void testDCC_Chain() throws Exception {
		exportTriple(api.getModel_MoflonJavaChain(), //
				api.getModel_MoflonDocChain(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDoc_MI_Run("moflonJavaChain", "moflonDocChain").run();
		testForConsistency(new JavaToDoc_CO_Run("moflonJavaChain", "moflonDocChain", solver).runCheckOnly(), 84);
	}

	@Test
	public void testDCC_MultiDel() throws Exception {
		exportTriple(api.getModel_MoflonJavaMultiDel(), //
				api.getModel_MoflonDocMultiDel(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDoc_MI_Run("moflonJavaMultiDel", "moflonDocMultiDel").run();
		testForConsistency(new JavaToDoc_CO_Run("moflonJavaMultiDel", "moflonDocMultiDel", solver).runCheckOnly(), 21);
	}
	
	@Test
	public void testDCC_Simple() throws Exception {
		exportTriple(api.getModel_MoflonJavaSimple(), //
				api.getModel_MoflonDocSimple(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDoc_MI_Run("moflonJavaSimple", "moflonDocSimple").run();
		testForConsistency(new JavaToDoc_CO_Run("moflonJavaSimple", "moflonDocSimple", solver).runCheckOnly(), 84);
	}
}
