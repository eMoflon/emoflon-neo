package org.emoflon.neo.example.javatodoc;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.emoflon.neo.api.org.emoflon.neo.example.javatodoc.API_JavaToDocTriplesForTesting;
import org.emoflon.neo.emsl.eMSL.Model;
import org.emoflon.neo.example.ENeoTest;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import JavaToDoc.run.JavaToDoc_BWD_Run;
import JavaToDoc.run.JavaToDoc_CO_Run;

public class BWD_CO_Tests extends ENeoTest {

	private API_JavaToDocTriplesForTesting api = new API_JavaToDocTriplesForTesting(builder);

	private void runTest(Model trgModel, String srcName, String trgName) throws Exception {
		builder.exportEMSLEntityToNeo4j(trgModel);
		new JavaToDoc_BWD_Run(srcName, trgName).run();
		assertTrue(new JavaToDoc_CO_Run(srcName, trgName).runCheckOnly().isConsistent());
	}

	@Ignore("Fixme: Result for BWD not unique")
	public void testTriple1() throws Exception {
		runTest(api.getModel_ConsistentTrg1(), "Source1", "ConsistentTrg1");
	}

	@Ignore("Fixme: Result for BWD not unique")
	public void testTriple2() throws Exception {
		runTest(api.getModel_ConsistentTrg2(), "Source2", "ConsistentTrg2");
	}

	@Ignore("Fixme: Result for BWD not unique")
	public void testTriple3() throws Exception {
		runTest(api.getModel_ConsistentTrg3(), "Source3", "ConsistentTrg3");
	}

	@Ignore("Fixme: Result for BWD not unique")
	public void testTriple4() throws Exception {
		runTest(api.getModel_ConsistentTrg4(), "Source4", "ConsistentTrg4");
	}
}
