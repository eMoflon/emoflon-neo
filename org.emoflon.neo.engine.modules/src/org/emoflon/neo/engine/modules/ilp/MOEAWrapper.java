package org.emoflon.neo.engine.modules.ilp;

import java.util.HashMap;
import java.util.Map;

import org.emoflon.neo.engine.ilp.ILPProblem;
import org.emoflon.neo.engine.ilp.ILPProblem.ILPSolution;
import org.emoflon.neo.engine.ilp.ILPSolver;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;

public final class MOEAWrapper extends ILPSolver{
	
	//MOEAProblem problem;
	
	/**
	 * Creates a new Gurobi ILP solver
	 *
	 * @param onlyBinaryVariables This setting defines the variable range of
	 *                            variables registered at Gurobi
	 */
	MOEAWrapper(final ILPProblem ilpProblem) {
		super(ilpProblem);
		//this.problem = (MOEAProblem)ilpProblem;
	}
	
//	protected MOEAWrapper(ILPProblem problem) {
//		super(problem);
//		this.problem = (MOEAProblem)problem;
		
		// create the optimization problem and evolutionary algorithm
		//moeaProblem = new MOEAProblem(ilpProblem);
		
//		properties = new Properties();
//		properties.setProperty("swap.rate", "0.7");
//		properties.setProperty("insertion.rate", "0.9");
//		properties.setProperty("pmx.rate", "0.4");
//		
//		algorithm = AlgorithmFactory.getInstance().getAlgorithm(
//				"GA", properties, problem);
		
		//this.onlyBinaryVariables = onlyBinaryVariables;
//	}

	@Override
	public ILPSolution solveILP() throws Exception {
		
		NondominatedPopulation result = new Executor()
				.withAlgorithm("NSGAII")
				.withProblem(ilpProblem)
				.withMaxEvaluations(100000)
				.run();
		
		Map<Integer,Integer> varAssignment = new HashMap<>();
		
		Solution solution = result.get(0); // for one Objective, this is the only solution
		boolean[] vA = EncodingUtils.getBinary(solution.getVariable(0));
		
		for (int i=0; i<vA.length; i++) {
			varAssignment.put(i+1, vA[i] ? 1 : 0);
		}
//		for (Solution solution : result) {
//			if (!solution.violatesConstraints()) {
//					System.out.format("%10.3f %10.3f%n",
//					solution.getObjective(0),
//					solution.getObjective(1));
//				}
//			}
		
		return ilpProblem.createILPSolution(varAssignment, solution.violatesConstraints(), solution.getObjective(0));
	}

}
