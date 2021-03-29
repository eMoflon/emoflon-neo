package org.emoflon.neo.engine.modules.ilp;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.emoflon.neo.engine.ilp.ILPProblem;
import org.emoflon.neo.engine.ilp.ILPProblem.ILPSolution;
import org.emoflon.neo.engine.ilp.ILPSolver;
import org.emoflon.neo.engine.ilp.MOEAProblem;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;

public final class MOEAWrapper extends ILPSolver{
	
	private int maxEvaluations;
	
	/**
	 * Creates a new Gurobi ILP solver
	 *
	 * @param onlyBinaryVariables This setting defines the variable range of
	 *                            variables registered at Gurobi
	 */
	public MOEAWrapper(final ILPProblem ilpProblem, int maxEvaluations) {
		super(ilpProblem);
		this.maxEvaluations = maxEvaluations;
	}

	@Override
	public ILPSolution solveILP() throws Exception {
		
		assert (ilpProblem instanceof MOEAProblem); //otherwise this wrapper doesn't make sense
		((MOEAProblem)ilpProblem).addObjective(ilpProblem.getObjective());
		
		// Genetic Algorihtm
//		NondominatedPopulation result = new Executor()
//				.withAlgorithm("GA")
//				.withProblem(ilpProblem)
//				.withMaxEvaluations(100 * ilpProblem.getVariables().size())
//				.run();
//		
//		Solution solution = result.get(0);
		
		// Simulated Annealing
		Solution solution = SimulatedAnnealing.solve((MOEAProblem)ilpProblem);
		
		// Random Sampling
//		Solution solution = RandomSampling.solve((MOEAProblem)ilpProblem);
		
		boolean[] vA = EncodingUtils.getBinary(solution.getVariable(0));
		Map<Integer,Integer> varAssignment = new HashMap<>();
		
		for (int j=0; j<vA.length; j++) {
			varAssignment.put(j+1, vA[j] ? 1 : 0);
		}
		
		ILPSolver.logger.info("MOEA-Framework found solution: " + solution.getObjective(0) + " - Feasible: " + !solution.violatesConstraints());
		
		return ilpProblem.createILPSolution(varAssignment, !solution.violatesConstraints(), solution.getObjective(0));
	}
	
	public Collection<ILPSolution> solve() throws Exception {
		
		Collection<ILPSolution> solutions = new HashSet<>();
		
		NondominatedPopulation result = new Executor()
				.withAlgorithm("NSGAII")
				.withProblem(ilpProblem)
				.withMaxEvaluations(maxEvaluations)
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
