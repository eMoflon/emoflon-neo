package org.emoflon.neo.example.javatodocsle.mi.in;

import org.emoflon.neo.api.javatodocsle.run.JavaToDocSLE_CO_Run;
import org.emoflon.neo.api.testsuitetgg.org.emoflon.neo.example.javatodocsle.mi.in.API_Geometry;
import org.emoflon.neo.api.testsuitetgg.org.emoflon.neo.example.javatodocsle.mi.performance.*;
import org.emoflon.neo.emf.handlers.EMFConverterHandler;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.Test;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
//import org.emoflon.neo.api.javatodocsle.run.JavaToDocSLE_CO_Run;
//import org.emoflon.neo.api.javatodocsle.run.JavaToDocSLE_MI_Run;
import org.eclipse.core.commands.ExecutionException;


public class GeometryTests extends ENeoTest {

	private API_Geometry api = new API_Geometry(builder);
	private API_JavaModel0 api_j0 = new API_JavaModel0(builder);
	private API_CreateCorrs0 api_c0 = new API_CreateCorrs0(builder);
	private API_DocModel0 api_d0 = new API_DocModel0(builder);
	private API_JavaModel1 api_j1 = new API_JavaModel1(builder);
	private API_CreateCorrs1 api_c1 = new API_CreateCorrs1(builder);
	private API_DocModel1 api_d1 = new API_DocModel1(builder);
	private API_JavaModel2 api_j2 = new API_JavaModel2(builder);
	private API_CreateCorrs2 api_c2 = new API_CreateCorrs2(builder);
	private API_DocModel2 api_d2 = new API_DocModel2(builder);
	private API_JavaModel3 api_j3 = new API_JavaModel3(builder);
	private API_CreateCorrs3 api_c3 = new API_CreateCorrs3(builder);
	private API_DocModel3 api_d3 = new API_DocModel3(builder);
	private API_JavaModel4 api_j4 = new API_JavaModel4(builder);
	private API_CreateCorrs4 api_c4 = new API_CreateCorrs4(builder);
	private API_DocModel4 api_d4 = new API_DocModel4(builder);
	
	private static final int nrOfIterations = 30;
	
	@Test
	public void testAll() throws Exception {
		for (int n=0; n<5; n++) {
			logger.info("###################################");
			logger.info("### Performance Test:  " + (n+1) + " Models ###");
			logger.info("###################################");
			Logger.getRootLogger().setLevel(Level.INFO);
//			logger.info("### Single test run with Gurobi ###");
//			testPerformance(n, SupportedILPSolver.Gurobi);
			//testAllOnce(SupportedILPSolver.Gurobi);
			
			Logger.getRootLogger().setLevel(Level.INFO);
			for (int i=0; i<nrOfIterations; i++) {
				logger.info("### Iteration " + i + " with Simulated Annealing ###");
				testPerformance(n, SupportedILPSolver.MOEA);
				//testAllOnce(SupportedILPSolver.MOEA);
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
		//Logger.getRootLogger().setLevel(Level.INFO);
		exportTriple(api.getModel_JavaGeometry13(), //
				api.getModel_DocGeometry2(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDocSLE_MI_Run("javaGeometry13", "docGeometry2").run(solver);
		//testForConsistency(new JavaToDocSLE_CO_Run("javaGeometry13", "docGeometry2", solver).runCheckOnly(), 34);
		clearDB();
	}
	
	public void test124() throws Exception {
		//Logger.getRootLogger().setLevel(Level.INFO);
		exportTriple(api.getModel_JavaGeometry14(), //
				api.getModel_DocGeometry2(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDocSLE_MI_Run("javaGeometry14", "docGeometry2").run(solver);
		//testForConsistency(new JavaToDocSLE_CO_Run("javaGeometry14", "docGeometry2", solver).runCheckOnly(), 34);
		clearDB();
	}
	
	public void test125() throws Exception {
		//Logger.getRootLogger().setLevel(Level.INFO);
		exportTriple(api.getModel_JavaGeometry25(), //
				api.getModel_DocGeometry1(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDocSLE_MI_Run("javaGeometry25", "docGeometry1").run(solver);
		//testForConsistency(new JavaToDocSLE_CO_Run("javaGeometry25", "docGeometry1", solver).runCheckOnly(), 34);
		clearDB();
	}
	
	public void test134() throws Exception {
		//Logger.getRootLogger().setLevel(Level.INFO);
		exportTriple(api.getModel_JavaGeometry13(), //
				api.getModel_DocGeometry4(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDocSLE_MI_Run("javaGeometry13", "docGeometry4").run(solver);
		//testForConsistency(new JavaToDocSLE_CO_Run("javaGeometry13", "docGeometry4", solver).runCheckOnly(), 34);
		clearDB();
	}
	
	public void test135() throws Exception {
		//Logger.getRootLogger().setLevel(Level.INFO);
		exportTriple(api.getModel_JavaGeometry35(), //
				api.getModel_DocGeometry1(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDocSLE_MI_Run("javaGeometry35", "docGeometry1").run(solver);
		//testForConsistency(new JavaToDocSLE_CO_Run("javaGeometry35", "docGeometry1", solver).runCheckOnly(), 34);
		clearDB();
	}
	
	public void test145() throws Exception {
		//Logger.getRootLogger().setLevel(Level.INFO);
		exportTriple(api.getModel_JavaGeometry14(), //
				api.getModel_DocGeometry5(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDocSLE_MI_Run("javaGeometry14", "docGeometry5").run(solver);
		//testForConsistency(new JavaToDocSLE_CO_Run("javaGeometry14", "docGeometry5", solver).runCheckOnly(), 34);
		clearDB();
	}
	
	public void test234() throws Exception {
		//Logger.getRootLogger().setLevel(Level.INFO);
		exportTriple(api.getModel_JavaGeometry24(), //
				api.getModel_DocGeometry3(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDocSLE_MI_Run("javaGeometry24", "docGeometry3").run(solver);
		//testForConsistency(new JavaToDocSLE_CO_Run("javaGeometry24", "docGeometry3", solver).runCheckOnly(), 34);
		clearDB();
	}
	
	public void test235() throws Exception {
		//Logger.getRootLogger().setLevel(Level.INFO);
		exportTriple(api.getModel_JavaGeometry25(), //
				api.getModel_DocGeometry3(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDocSLE_MI_Run("javaGeometry25", "docGeometry3").run(solver);
		//testForConsistency(new JavaToDocSLE_CO_Run("javaGeometry25", "docGeometry3", solver).runCheckOnly(), 34);
		clearDB();
	}
	
	public void test245() throws Exception {
		//Logger.getRootLogger().setLevel(Level.INFO);
		exportTriple(api.getModel_JavaGeometry24(), //
				api.getModel_DocGeometry5(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDocSLE_MI_Run("javaGeometry24", "docGeometry5").run(solver);
		//testForConsistency(new JavaToDocSLE_CO_Run("javaGeometry24", "docGeometry5", solver).runCheckOnly(), 34);
		clearDB();
	}
	
	public void test345() throws Exception {
		//Logger.getRootLogger().setLevel(Level.INFO);
		exportTriple(api.getModel_JavaGeometry35(), //
				api.getModel_DocGeometry4(), //
				api.getRule_CreateCorrs().rule());
		new JavaToDocSLE_MI_Run("javaGeometry35", "docGeometry4").run(solver);
		//testForConsistency(new JavaToDocSLE_CO_Run("javaGeometry35", "docGeometry4", solver).runCheckOnly(), 34);
		clearDB();
	}
	
	public void testPerformance(int n, SupportedILPSolver s) throws Exception {
		solver = s;
		
		// Export models
		switch (n) {
		case 4:
			exportTriple(api_j4.getModel_MoflonJava4(), //
					api_d4.getModel_MoflonDoc4());
			break;
		case 3: 
			exportTriple(api_j3.getModel_MoflonJava3(), //
					api_d3.getModel_MoflonDoc3());
			break;
		case 2:
			exportTriple(api_j2.getModel_MoflonJava2(), //
					api_d2.getModel_MoflonDoc2());
			break;
		case 1:
			exportTriple(api_j1.getModel_MoflonJava1(), //
					api_d1.getModel_MoflonDoc1());
			break;
		case 0:
			exportTriple(api_j0.getModel_MoflonJava0(), //
					api_d0.getModel_MoflonDoc0());
			break;
		}
		
		// Export rules
		switch (n) {
		case 4:
			api_c4.getRule_CreateClazzToDoc4().rule().apply();
			api_c4.getRule_CreateMethodToEntry4().rule().apply();
			api_c4.getRule_CreateFieldToEntry4().rule().apply();
		case 3: 
			api_c3.getRule_CreateClazzToDoc3().rule().apply();
			api_c3.getRule_CreateMethodToEntry3().rule().apply();
			api_c3.getRule_CreateFieldToEntry3().rule().apply();
		case 2:
			api_c2.getRule_CreateClazzToDoc2().rule().apply();
			api_c2.getRule_CreateMethodToEntry2().rule().apply();
			api_c2.getRule_CreateFieldToEntry2().rule().apply();
		case 1:
			api_c1.getRule_CreateClazzToDoc1().rule().apply();
			api_c1.getRule_CreateMethodToEntry1().rule().apply();
			api_c1.getRule_CreateFieldToEntry1().rule().apply();
		case 0:
			api_c0.getRule_CreateClazzToDoc0().rule().apply();
			api_c0.getRule_CreateMethodToEntry0().rule().apply();
			api_c0.getRule_CreateFieldToEntry0().rule().apply();
		}

		
		new JavaToDocSLE_MI_Run("moflonJava" + n, "moflonDoc" + n).run(solver);
		//new JavaToDocSLE_CO_Run("moflonJava0", "moflonDoc0").run();
		clearDB();
	}
}
