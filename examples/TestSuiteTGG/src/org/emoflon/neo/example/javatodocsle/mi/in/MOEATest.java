package org.emoflon.neo.example.javatodocsle.mi.in;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.emoflon.neo.engine.ilp.ILPProblem.Comparator;
import org.emoflon.neo.engine.ilp.ILPProblem.ILPLinearExpression;
import org.emoflon.neo.engine.ilp.ILPProblem.ILPObjective;
import org.emoflon.neo.engine.ilp.ILPProblem.ILPSolution;
import org.emoflon.neo.engine.ilp.ILPProblem.Objective;
import org.emoflon.neo.engine.ilp.MOEAProblem;
import org.emoflon.neo.engine.modules.ilp.MOEAWrapper;
import org.junit.jupiter.api.Test;

public class MOEATest {
	
	// Create Coefficients
	int[][] norm = {
			{3, 3, 3, 5, 5, 5, 3, 5, 1, 5, 5, 5, 5, 0, 0, 0, 0, 3, 3, 0, 3},
			{3, 3, 3, 5, 5, 5, 3, 5, 1, 5, 3, 3, 5, 0, 0, 0, 0, 3, 3, 3, 3},
			{1, 3, 3, 4, 3, 3, 3, 5, 1, 5, 5, 5, 5, 0, 0, 0, 0, 3, 3, 0, 3},
			{3, 3, 3, 4, 3, 3, 5, 5, 3, 5, 3, 3, 5, 0, 0, 0, 0, 3, 0, 3, 3},
			{1, 3, 3, 4, 3, 3, 5, 5, 3, 5, 5, 5, 5, 0, 0, 0, 0, 3, 0, 0, 3},
			{1, 3, 3, 4, 3, 3, 5, 5, 3, 5, 3, 3, 5, 0, 0, 0, 0, 3, 0, 3, 3},
			{3, 3, 3, 5, 5, 5, 3, 5, 3, 5, 3, 3, 5, 0, 0, 0, 0, 0, 3, 3, 3},
			{1, 3, 3, 4, 3, 3, 3, 5, 3, 5, 5, 5, 5, 0, 0, 0, 0, 0, 3, 0, 3},
			{1, 3, 3, 4, 3, 3, 3, 5, 3, 5, 3, 3, 5, 0, 0, 0, 0, 0, 3, 3, 3},
			{1, 3, 3, 4, 3, 3, 5, 5, 5, 5, 3, 3, 5, 0, 0, 0, 0, 0, 0, 3, 3}
	};
	
	int[][] alpha = {
			{0, 0, 0, 0, 0, 0, 2, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{0, 0, 0, 0, 0, 0, 2, 0, 4, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{2, 0, 0, 1, 2, 2, 2, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{0, 0, 0, 1, 2, 2, 0, 0, 2, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{2, 0, 0, 1, 2, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{2, 0, 0, 1, 2, 2, 0, 0, 2, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{0, 0, 0, 0, 0, 0, 2, 0, 2, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{2, 0, 0, 1, 2, 2, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{2, 0, 0, 1, 2, 2, 2, 0, 2, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{2, 0, 0, 1, 2, 2, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0}
	};
	
	int[][] beta = {
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2, 1, 1, 0, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 1, 1, 1, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 1, 1, 0, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 2, 1, 0, 1, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 1, 0, 0, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2, 0, 1, 0, 1, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 2, 0, 1, 1, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 0, 1, 0, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2, 0, 0, 1, 1, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 0, 0, 1, 0}
	};
	
	int[][] gamma = {
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 3, 1, 1, 0, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 1, 1, 1, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 1, 1, 0, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 0, 3, 1, 0, 1, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 3, 1, 0, 0, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 3, 0, 1, 0, 1, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 0, 3, 0, 1, 1, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 3, 0, 1, 0, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 3, 0, 0, 1, 1, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 3, 3, 0, 0, 1, 0}
	};
	
	@Test
	public void testMultipleObjectives() throws Exception {
		
		for (int i=0; i<10; i++) {
				
			MOEAProblem p = new MOEAProblem();	
			setConstraints(p);		
			
			setObjective(p, norm[i], Objective.maximize); 
			setObjective(p, alpha[i], Objective.minimize); 
			setObjective(p, beta[i], Objective.maximize); 
			setObjective(p, gamma[i], Objective.minimize); 
			
			Collection<ILPSolution> solutions = (new MOEAWrapper(p)).solve();
			
			for (ILPSolution s : solutions) {
				printSolutionInformation(p, s, i);
			}
		}
		
	}

	private void setObjective(MOEAProblem p, int[] coefficients, Objective obj) {
		ILPLinearExpression linexpr = p.new ILPLinearExpression();
		
		int i=0;
		for (int c : coefficients) {
			linexpr.addTerm("x" + ++i, c);
		}
		
		ILPObjective o = p.new ILPObjective(linexpr, obj);
		p.addObjective(o);
		
		
	}
	
	private void printSolutionInformation(MOEAProblem p, ILPSolution solution, int i) {
		double norm, alpha, beta, gamma;
		norm = alpha = beta = gamma = 0;
		String chosenVariables = "";
		
		for (String var : p.getVariables()) {
			if (solution.getVariable(var) > 0) {
				chosenVariables += var + ", ";
				int idx = Integer.valueOf(var.split("x")[1]);
				norm += this.norm[i][idx-1];
				alpha += this.alpha[i][idx-1];
				beta += this.beta[i][idx-1];
				gamma += this.gamma[i][idx-1];
			}
		}
		
		Logger.getRootLogger().info("Solution for Problem " + i + ": Objective function values: " + norm + ", " + alpha + 
				" alpha, " + beta + " beta, " + gamma + " gamma.") ;
		Logger.getRootLogger().info("Chosen variables: " + chosenVariables);
	}
	
	private void setConstraints(MOEAProblem p) {
		
		int[][][] constraints = {
				{{1}, {2,18,19}, {}},
				{{1}, {3,20}, {}},
				{{1}, {4,21}, {}},
				{{0}, {5}, {1}},	
				{{0}, {6}, {1}},	
				{{0}, {7}, {2,18,19}},
				{{0}, {8}, {2,18,19}},
				{{0}, {9}, {2,18,19}},	
				{{0}, {10}, {3,20}},	
				{{0}, {11}, {3,20}},	
				{{0}, {12}, {3,20}},		
				{{0}, {13}, {4,21}},	
				{{0}, {14}, {1}},		
				{{0}, {15}, {4,21}},	
				{{0}, {16}, {4,21}},	
				{{0}, {17}, {4,21}},	
				{{0}, {18}, {1}},		
				{{0}, {19}, {4,21}},	
				{{0}, {20}, {1}},	 	
				{{2}, {4,5,16}, {}},						
				{{2}, {4,14,17}, {}},
				{{2}, {5,9,18}, {}},
				{{2}, {8,15,19}, {}},
				{{2}, {9,16,19}, {}},
				{{2}, {5,11,20}, {}},
				{{2}, {6,12,20}, {}}
		};
		
		for (int[][] c : constraints) {
			ILPLinearExpression linexpr = p.new ILPLinearExpression();
			
			for (int v : c[1]) {
				linexpr.addTerm("x" + v, 1);
			}
			
			for (int v : c[2]) {
				linexpr.addTerm("x" + v, -1);
			}
			
			p.addConstraint(linexpr, Comparator.le, c[0][0], "");
		}
	}
}
