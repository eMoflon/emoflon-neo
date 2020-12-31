package org.emoflon.neo.engine.modules.ilp;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
	public MOEAWrapper(final ILPProblem ilpProblem) {
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
		
		throw new UnsupportedOperationException("For multi-objective optimization, there might be multiple solutions! Use 'solve()' instead!");
	}
	
	public Collection<ILPSolution> solve() throws Exception {
		
		Collection<ILPSolution> solutions = new HashSet<>();
		
		NondominatedPopulation result = new Executor()
				.withAlgorithm("NSGAII")
				.withProblem(ilpProblem)
				.withMaxEvaluations(100000)
				.run();
		
		for (int i = 0; i<result.size(); i++) {
			Map<Integer,Integer> varAssignment = new HashMap<>();
			
			Solution solution = result.get(i);
			boolean[] vA = EncodingUtils.getBinary(solution.getVariable(0));
			
			for (int j=0; j<vA.length; j++) {
				varAssignment.put(j+1, vA[j] ? 1 : 0);
			}
			
			solutions.add(ilpProblem.createILPSolution(varAssignment, solution.violatesConstraints(), solution.getObjective(0)));
		}
		
		return solutions;
	}
}
