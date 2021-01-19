package org.emoflon.neo.engine.modules.ilp;

import java.util.Random;

import org.apache.log4j.Logger;
import org.emoflon.neo.engine.ilp.MOEAProblem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.BinaryVariable;

public class SimulatedAnnealing {
	
	enum Scheme {
		LINEAR, QUADRATIC, GEOMETRIC;
	}
	
	//Scheme which is currently used
	private static final Scheme scheme = Scheme.LINEAR;
	
	/**
	  * computation of parameter T
	  * @param T: old value of T
	  * @param t: number of the run
	  * @param scheme: cooling scheme
	  * @return: new value of T
	  */
	 private static double cool(double T0, double t, Scheme scheme, int maxEvaluations) {
		 switch (scheme) {
		 case GEOMETRIC: 
			 return Math.pow(0.999, t) * T0;
		 case LINEAR:
			 // decrease T linearly, but return at least 0
			 return Math.max(T0 * (maxEvaluations - t) / maxEvaluations, 0);
		 case QUADRATIC:
			 return T0 / Math.pow((t + 1),2);
	     default:
	    	 return T0;
		 }
	 }
	 
	 /**
	  * implements the Simulated Annealing algorithm
	  * @param minTour: minimal tour found so far
	  * @param maxEvaluations: Stop after n improvement steps 	
	  * @return: number of tours generated
	  */
	 public static Solution solve(MOEAProblem p) {
		 
		 double T0 = 0;
		 
		 for (String v : p.getVariables())
			 T0 += Math.abs(p.getObjective().getLinearExpression().getCoefficient(p.getVariableId(v)));

		 double T = T0;
		 int t = 1;
		 Random rnd = new Random();
		 int cc = (int)Math.sqrt(p.getVariables().size());
		 int cb = p.getVariables().size();
		 int maxEvaluations = 200 * p.getVariables().size();
				 
		 Solution currentSolution = p.newSolution();
		 Solution bestSolution = p.newSolution();
		 Solution neighbourSolution = p.newSolution();
		 
		 BinaryVariable bv = new BinaryVariable(p.getVariables().size());
		 
		 for (int i=0; i<bv.getNumberOfBits(); i++)
			 bv.set(i, false);
		 
		 bestSolution.setVariable(0, bv);
		 p.evaluate(bestSolution);
		 double bestValue = bestSolution.getObjective(0) + (bestSolution.violatesConstraints() ? T0 : 0);
		 
		 currentSolution.setVariable(0, bv);
		 p.evaluate(currentSolution);
		 double currentValue = currentSolution.getObjective(0) + (currentSolution.violatesConstraints() ? T0 : 0);
		 
		 double neighbourValue;
		 
		 int nb = 0, nc = 0;
		 
		 for (int i=0; i<maxEvaluations; i++) {
			 
			 bv = (BinaryVariable)currentSolution.getVariable(0).copy();
			 
	// Pertubation
			 int index = rnd.nextInt(bv.getNumberOfBits());
			 bv.set(index, !bv.get(index));
			 neighbourSolution.setVariable(0, bv);
			 p.evaluate(neighbourSolution);
			 neighbourValue = neighbourSolution.getObjective(0) + (neighbourSolution.violatesConstraints() ? T0 : 0); 
			 
	// Accept solution with a certain probability		 
			 if (neighbourValue < bestValue) {
				 bestSolution = neighbourSolution.copy();
				 bestValue = neighbourValue;
				 currentSolution = neighbourSolution.copy();
				 currentValue = neighbourValue;
				 nb = nc = 0;
				 Logger.getRootLogger().debug("New optimum solution found after step " + i + " of " + maxEvaluations);
			 }
			 else if (neighbourValue < currentValue) {
				 currentSolution = neighbourSolution.copy();
				 currentValue = neighbourValue;
				 nc = 0;
			 }
			 else {
				 nc++;
				 if (rnd.nextDouble() <= Math.exp(-(neighbourValue - currentValue)/T)) {
					 currentSolution = neighbourSolution.copy();
					 currentValue = neighbourValue;
				 }
			 }
			 
			 if (nb > cb || nc > cc) {
				 currentSolution = bestSolution.copy();
				 currentValue = bestValue;
				 nb = nc = 0;
			 }

	// Cooling
			 T = cool(T0,t,scheme,maxEvaluations);
			 t++;
			 nb++;	 
		 }
		 
		 return bestSolution;
	 }
}
