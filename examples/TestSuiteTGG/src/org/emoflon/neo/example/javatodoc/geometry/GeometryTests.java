package org.emoflon.neo.example.javatodoc.geometry;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.testsuitetgg.org.emoflon.neo.example.javatodoc.geometry.API_Geometry;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
import org.emoflon.neo.example.ENeoTest;
import org.emoflon.neo.example.javatodoc.performance.JavaToDocSLE_MI_Run;
import org.junit.Ignore;


public class GeometryTests extends ENeoTest {

	private API_Geometry api = new API_Geometry(builder);
	
	private static final int nrOfIterations = 30;
	
	@Ignore("Performance Tests are not part of the test suite.")
	public void testAll() throws Exception {
		for (int n=0; n<5; n++) {
			logger.info("###################################");
			logger.info("### Performance Test:  " + (n+1) + " Models ###");
			logger.info("###################################");
			Logger.getRootLogger().setLevel(Level.INFO);
			logger.info("### Single test run with Gurobi ###");
			testAllOnce(SupportedILPSolver.Gurobi);
			
			Logger.getRootLogger().setLevel(Level.INFO);
			for (int i=0; i<nrOfIterations; i++) {
				logger.info("### Iteration " + i + " with Simulated Annealing ###");
				testAllOnce(SupportedILPSolver.MOEA);
			}
		}
	}
	
	private void testAllOnce(SupportedILPSolver s) throws Exception {
		solver = s;
		logger.warn("### Test configuration 123 ###");
		test123();
		logger.warn("### Test configuration 124 ###");
		test124();
		logger.warn("### Test configuration 125 ###");
		test125();
		logger.warn("### Test configuration 134 ###");
		test134();
		logger.warn("### Test configuration 135 ###");
		test135();
		logger.warn("### Test configuration 145 ###");
		test145();
		logger.warn("### Test configuration 234 ###");
		test234();
		logger.warn("### Test configuration 235 ###");
		test235();
		logger.warn("### Test configuration 245 ###");
		test245();
		logger.warn("### Test configuration 345 ###");
		test345();
	}
	
	public void test123() throws Exception {
		exportTriple(api.getModel_JavaGeometry13(), //
				api.getModel_DocGeometry2(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDocSLE_MI_Run("javaGeometry13", "docGeometry2").run(solver);
		clearDB();
	}
	
	public void test124() throws Exception {
		exportTriple(api.getModel_JavaGeometry14(), //
				api.getModel_DocGeometry2(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDocSLE_MI_Run("javaGeometry14", "docGeometry2").run(solver);
		clearDB();
	}
	
	public void test125() throws Exception {
		exportTriple(api.getModel_JavaGeometry25(), //
				api.getModel_DocGeometry1(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDocSLE_MI_Run("javaGeometry25", "docGeometry1").run(solver);
		clearDB();
	}
	
	public void test134() throws Exception {
		exportTriple(api.getModel_JavaGeometry13(), //
				api.getModel_DocGeometry4(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDocSLE_MI_Run("javaGeometry13", "docGeometry4").run(solver);
		clearDB();
	}
	
	public void test135() throws Exception {
		exportTriple(api.getModel_JavaGeometry35(), //
				api.getModel_DocGeometry1(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDocSLE_MI_Run("javaGeometry35", "docGeometry1").run(solver);
		clearDB();
	}
	
	public void test145() throws Exception {
		exportTriple(api.getModel_JavaGeometry14(), //
				api.getModel_DocGeometry5(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDocSLE_MI_Run("javaGeometry14", "docGeometry5").run(solver);
		clearDB();
	}
	
	public void test234() throws Exception {
		exportTriple(api.getModel_JavaGeometry24(), //
				api.getModel_DocGeometry3(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDocSLE_MI_Run("javaGeometry24", "docGeometry3").run(solver);
		clearDB();
	}
	
	public void test235() throws Exception {
		exportTriple(api.getModel_JavaGeometry25(), //
				api.getModel_DocGeometry3(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDocSLE_MI_Run("javaGeometry25", "docGeometry3").run(solver);
		clearDB();
	}
	
	public void test245() throws Exception {
		exportTriple(api.getModel_JavaGeometry24(), //
				api.getModel_DocGeometry5(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDocSLE_MI_Run("javaGeometry24", "docGeometry5").run(solver);
		clearDB();
	}
	
	public void test345() throws Exception {
		exportTriple(api.getModel_JavaGeometry35(), //
				api.getModel_DocGeometry4(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDocSLE_MI_Run("javaGeometry35", "docGeometry4").run(solver);
		clearDB();
	}
}
