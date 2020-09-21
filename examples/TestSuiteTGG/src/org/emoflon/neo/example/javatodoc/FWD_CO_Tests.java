package org.emoflon.neo.example.javatodoc;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.emoflon.neo.api.org.emoflon.neo.example.javatodoc.API_JavaToDocTriplesForTesting;
import org.emoflon.neo.emsl.eMSL.Model;
import org.emoflon.neo.example.ENeoTest;
import org.junit.Ignore;
import JavaToDoc.run.JavaToDoc_CO_Run;
import JavaToDoc.run.JavaToDoc_FWD_Run;

public class FWD_CO_Tests extends ENeoTest {

	private API_JavaToDocTriplesForTesting api = new API_JavaToDocTriplesForTesting(builder);

	private void runTest(Model srcModel, String srcName, String trgName) throws Exception {
		builder.exportEMSLEntityToNeo4j(srcModel);
		new JavaToDoc_FWD_Run(srcName, trgName).run();
		assertTrue(new JavaToDoc_CO_Run(srcName, trgName).runCheckOnly().isConsistent());
	}

	@Ignore("Non-deterministic, as there are no filter NACs implemented yet.")
	public void testTriple1() throws Exception {
		runTest(api.getModel_ConsistentSrc1(), "ConsistentSrc1", "Target1");
	}

	@Ignore("Non-deterministic, as there are no filter NACs implemented yet.")
	public void testTriple2() throws Exception {
		runTest(api.getModel_ConsistentSrc2(), "ConsistentSrc2", "Target2");
	}

	@Ignore("Non-deterministic, as there are no filter NACs implemented yet.")
	public void testTriple3() throws Exception {
		runTest(api.getModel_ConsistentSrc3(), "ConsistentSrc3", "Target3");
	}

	@Ignore("Non-deterministic, as there are no filter NACs implemented yet.")
	public void testTriple4() throws Exception {
		runTest(api.getModel_ConsistentSrc4(), "ConsistentSrc4", "Target4");
	}
}
