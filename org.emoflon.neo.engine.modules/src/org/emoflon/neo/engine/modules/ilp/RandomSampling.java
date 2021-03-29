package org.emoflon.neo.engine.modules.ilp;

import org.apache.log4j.Logger;
import org.emoflon.neo.engine.ilp.MOEAProblem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.BinaryVariable;

public class RandomSampling {

	public static Solution solve(MOEAProblem p) {
		BinaryVariable bv = new BinaryVariable(p.getVariables().size());
		
		Solution currentSolution = p.newSolution();
		Solution bestSolution = p.newSolution();
		double bestValue = Integer.MIN_VALUE;
		double currentValue;
		int nrOfFeasibleSolutions = 0;
		
		for (int i=0; i<p.getVariables().size() * 100; i++) {
			bv.randomize();
			currentSolution.setVariable(0,bv);
			p.evaluate(currentSolution);
			currentValue = currentSolution.getObjective(0);
			if (!currentSolution.violatesConstraints() && currentValue < bestValue) {
				bestSolution = currentSolution.copy();
				bestValue = currentValue;
			}
			if (!currentSolution.violatesConstraints())
				nrOfFeasibleSolutions++;
		}
		
		Logger.getRootLogger().info("Number of evaluations: " + p.getVariables().size() * 100);
		Logger.getRootLogger().info("Number of feasible solutions: " + nrOfFeasibleSolutions);
		return bestSolution;
	}
}
