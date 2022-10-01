package org.emoflon.neo.example.javatodoc;

import java.util.Collection;
import org.apache.log4j.Logger;
import org.emoflon.neo.engine.ilp.ILPProblem.Comparator;
import org.emoflon.neo.engine.ilp.ILPProblem.ILPLinearExpression;
import org.emoflon.neo.engine.ilp.ILPProblem.ILPObjective;
import org.emoflon.neo.engine.ilp.ILPProblem.ILPSolution;
import org.emoflon.neo.engine.ilp.ILPProblem.Objective;
import org.emoflon.neo.engine.ilp.MOEAProblem;
import org.emoflon.neo.engine.modules.ilp.MOEAWrapper;
import org.junit.Ignore;

public class MOEATest {

	public static final int NR_OF_TEST_RUNS = 1; //increase for statistical tests
	
	// Create Coefficients
	int[][] norm = {
			{3, 3, 3, 5, 5, 5, 3, 5, 1, 5, 5, 5, 5, 0, 0, 0, 0, 3, 3, 0, 3},
			{3, 3, 3, 5, 5, 5, 3, 5, 1, 5, 3, 3, 5, 0, 0, 0, 0, 3, 3, 3, 3},
			{1, 3, 3, 4, 3, 3, 3, 5, 1, 5, 5, 5, 5, 0, 0, 0, 0, 3, 3, 0, 3},
			{3, 3, 3, 5, 5, 5, 5, 5, 3, 5, 3, 3, 5, 0, 0, 0, 0, 3, 0, 3, 3},
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
			{0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0},
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
	
	int[] config = {123, 124, 125, 134, 135, 145, 234, 235, 245, 345};
	
	@Ignore("Not part of the correctness test suite")
	public void testMultipleObjectives() throws Exception {
		for (int j=0; j<NR_OF_TEST_RUNS; j++) {
			Logger.getRootLogger().info("### Start of test run " + j + " ###");
			for (int i=0; i<10; i++) {
				
				Logger.getRootLogger().info("Determine solutions for config: " + config[i]);
				MOEAProblem p = new MOEAProblem();	
				setConstraints(p);		
				disableVariables(p,i);
				
				setObjective(p, norm[i], Objective.maximize); 
				setObjective(p, alpha[i], Objective.minimize); 
				setObjective(p, beta[i], Objective.maximize); 
				setObjective(p, gamma[i], Objective.minimize); 
				
				Collection<ILPSolution> solutions = (new MOEAWrapper(p, p.getVariables().size() * 1000)).solve();
				
				Logger.getRootLogger().info("Number of solutions: " + solutions.size());
				for (ILPSolution s : solutions) {
					printSolutionInformation(p, s, i);
				}
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
		assert alpha[i].length == beta[i].length && 
			   gamma[i].length == norm[i].length && 
			   beta[i].length == gamma[i].length;
		
		double norm, alpha, beta, gamma;
		norm = alpha = beta = gamma = 0;
		String[] chosenVariables = new String[p.getVariables().size()];
		
		for (String var : p.getVariables()) {
			if (solution.getVariable(var) > 0) {
				int idx = Integer.valueOf(var.split("x")[1]);
				if (idx > this.alpha[i].length)
					continue; // variable only used in constraints
				norm += this.norm[i][idx-1];
				alpha += this.alpha[i][idx-1];
				beta += this.beta[i][idx-1];
				gamma += this.gamma[i][idx-1];
				chosenVariables[idx] = var;
			}
		}
		
		String varString = "";
		for (String cv : chosenVariables) {
			if (cv != null)
				varString += cv;
			varString += "; ";
		}
		Logger.getRootLogger().info("Solution for Problem " + i + ": Objective function values (norm, alpha, beta, gamma) ;" + norm + ";" + alpha + 
				";" + beta + ";" + gamma + "; Chosen variables ;" + varString) ;
	}
	
	private void disableVariables(MOEAProblem p, int i) {
		assert alpha[i].length == beta[i].length && gamma[i].length == norm[i].length && beta[i].length == gamma[i].length;
		
		for (int j=0; j<alpha[i].length; j++) {
			if (alpha[i][j] == 0 && beta[i][j] == 0 && gamma[i][j] == 0 && norm[i][j] == 0) {
				p.fixVariable("x" + (j+1), false);
			}
		}
	}
	
	private void setConstraints(MOEAProblem p) {
		
		// Form: \sum{ b \in B} - \sum { c \in C} \leq a
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
				{{2}, {6,12,20}, {}},
				{{2}, {7,13,19}, {}},
				{{3}, {4,5,9,19}, {}},
				{{0}, {5}, {6}},
				{{0}, {6}, {5}},
				{{0}, {14}, {6}},
				{{0}, {14}, {5}},
				
				{{0}, {2}, {7}},
				{{0}, {2}, {8}},
				{{0}, {2}, {9}},
				{{0}, {18}, {7}},
				{{0}, {18}, {8}},
				{{0}, {18}, {9,5,16}},
				{{0}, {18}, {8}},
				{{0}, {19}, {7,13}},
				{{0}, {4}, {13}},
				{{0}, {4}, {5,16}},
				{{0}, {21}, {13}},
				{{0}, {21}, {16}},
				{{0}, {3}, {10}},
				{{0}, {3}, {11}},
				{{0}, {3}, {12}},
				{{0}, {20}, {10}},
				{{0}, {20}, {11,5}},
				{{0}, {20}, {12,6}},
				
				{{0}, {7}, {9,22,23,24}},
				{{0}, {22,22,22}, {5,6,20}},
				{{0}, {23}, {16,19}},
				{{0}, {24,24,24,24}, {19,4,5,6}},
				
				{{0}, {8}, {9,25,26,27,28,29,30}},
				{{0}, {25,25,25}, {5,14,20}},
				{{0}, {26,26,26}, {16,17,19}},
				{{0}, {27,27,27,27}, {19,4,5,14}},
				{{0}, {28,28,28,28}, {19,4,5,17}},
				{{0}, {29,29,29,29}, {19,4,16,14}},
				{{0}, {30,30,30,30}, {19,4,16,17}},
				
				{{0}, {13}, {16,31}},
				{{0}, {31}, {4,5,6}},
				
				{{0}, {15}, {32,33,34,35,36}},
				{{0}, {32,32}, {16,17}},
				{{0}, {33,33,33}, {4,5,14}},
				{{0}, {34,34,34}, {4,5,17}},
				{{0}, {35,35,35}, {4,16,14}},
				{{0}, {36,36,36}, {4,16,17}},
				
				{{0}, {10}, {37,38,39,40,41}},
				{{0}, {37,37}, {11,12}},
				{{0}, {38,38,38}, {20,5,14}},
				{{0}, {39,39,39}, {20,5,12}},
				{{0}, {40,40,40}, {20,11,14}},
				{{0}, {41,41,41}, {20,11,12}}
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
