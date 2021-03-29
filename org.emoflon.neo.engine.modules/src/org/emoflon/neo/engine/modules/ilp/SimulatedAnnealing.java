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
	private static int maxEvaluations;
	
	/**
	  * computation of parameter T
	  * @param T: old value of T
	  * @param t: number of the run
	  * @param scheme: cooling scheme
	  * @return: new value of T
	  */
	 private static double cool(double T0, double T, Scheme scheme) {
		 switch (scheme) {
		 case GEOMETRIC: 
			 return T *= 0.98;
		 case LINEAR:
			 // decrease T linearly, but return at least 0
			 return T - T0 / maxEvaluations;
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
		 double factor = 0;
		 
		 for (String v : p.getVariables())
			 T0 += p.getObjective().getLinearExpression().getCoefficient(p.getVariableId(v)) < 0 ? 0 : 
				 p.getObjective().getLinearExpression().getCoefficient(p.getVariableId(v));

		 double T = T0;
		 Random rnd = new Random();
		 int cc = 3; //(int)Math.pow(scale,0.5);
		 int cb = 10; //(int)Math.pow(scale,1);5
		 maxEvaluations = 1 * p.getVariables().size();
				 
		 Solution currentSolution = p.newSolution();
		 Solution bestSolution = p.newSolution();
		 Solution neighbourSolution = p.newSolution();
		 
		 BinaryVariable bv = new BinaryVariable(p.getVariables().size());
		 
		 for (int i=0; i<bv.getNumberOfBits(); i++)
			 bv.set(i, false);
		 
		 bestSolution.setVariable(0, bv.copy());
		 p.evaluate(bestSolution);
		 factor = 0;
		 for (double c : bestSolution.getConstraints()) {
			 factor += c;
		 }
		 double bestValue = bestSolution.getObjective(0) + factor * T0; //(bestSolution.violatesConstraints() ? T0 : 0);
		 
		 currentSolution.setVariable(0, bv.copy());
		 p.evaluate(currentSolution);
		 factor = 0;
		 for (double c : currentSolution.getConstraints()) {
			 factor += c;
		 }
		 double currentValue = currentSolution.getObjective(0) + factor * T0; //(currentSolution.violatesConstraints() ? T0 : 0);
		 
		 double neighbourValue;

		 int nb = 0, nc = 0, nrOfRestarts = 0;
		 
		 for (int i=0; i<maxEvaluations; i++) {
			 
			 bv = (BinaryVariable)currentSolution.getVariable(0).copy();
			 
	// Pertubation
			 double rand = rnd.nextDouble();
			 int nrOfFlips = -(int)(Math.log(1-rand) /Math.log(2));
			 
			 for (int j=0; j<nrOfFlips; j++) {
				 int index = rnd.nextInt(bv.getNumberOfBits());
				 bv.set(index, !bv.get(index));
			 }
			 neighbourSolution.setVariable(0, bv);
			 p.evaluate(neighbourSolution);
			 factor = 0;
			 for (double c : neighbourSolution.getConstraints()) {
				 factor += c;
			 }
			 neighbourValue = neighbourSolution.getObjective(0) + factor * T0; //(neighbourSolution.violatesConstraints() ? T0 : 0); 
			 
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
	// Restart		 
			 if (nb > cb || nc > cc) {
				 currentSolution = bestSolution.copy();
				 currentValue = bestValue;
				 nb = nc = 0;
				 nrOfRestarts ++;
			 }

	// Cooling
			 T = cool(T0,T,scheme);
			 
			 if (T < 1) {
				 // Re-Heating
				 T = T0;
			 }
			 nb++;	 
		 }
		 
		 Logger.getRootLogger().info("Number of restarts: " + nrOfRestarts);
		 return bestSolution;
	 }
}
