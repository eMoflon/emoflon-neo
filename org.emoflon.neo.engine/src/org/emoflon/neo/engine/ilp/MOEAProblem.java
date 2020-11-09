package org.emoflon.neo.engine.ilp;

import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;

public class MOEAProblem extends BinaryILPProblem implements Problem {

	@Override
	public void evaluate(Solution solution) {
		
		boolean[] x = EncodingUtils.getBinary(solution.getVariable(0));
		
		// Objective Function
		double ofv = 0;
		ILPLinearExpression le = getObjective().getLinearExpression();
		
		for (String v : getVariables()) {
			ofv += le.getCoefficient(getVariableId(v)) * (x[getVariableId(v)-1] ? 1 : 0);
		}
		
		if (objective.getObjectiveOperation().equals(Objective.minimize))
			solution.setObjective(0, ofv);
		else if (objective.getObjectiveOperation().equals(Objective.maximize))
			solution.setObjective(0, -ofv);
		else 
			throw new IllegalArgumentException("Unsupported objective: " + objective.getObjectiveOperation().toString());
		
		// Constraints
		int constraintCounter = 0;
		for (ILPConstraint c : getConstraints()) {
			ILPLinearExpression cle = c.getLinearExpression();
			double exprValue = 0;
			for (int v : cle.getVariables()) {
				exprValue += cle.getCoefficient(v) * (x[v-1] ? 1 : 0);
			}
			
			switch(c.getComparator()) {
				case ge:
					solution.setConstraint(constraintCounter++, exprValue >= c.getValue() ? 0.0 : c.getValue() - exprValue); break;
				case le:
					solution.setConstraint(constraintCounter++, exprValue <= c.getValue() ? 0.0 : exprValue - c.getValue()); break;
				case eq:
					solution.setConstraint(constraintCounter++, exprValue == c.getValue() ? 0.0 : Math.abs(exprValue - c.getValue())); break;
				default:
					throw new IllegalArgumentException("Unsupported comparator: " + c.getComparator().toString());
			}	
		}
	}

	@Override
	public Solution newSolution() {
		Solution solution = new Solution(1,1,getNumberOfConstraints());
		solution.setVariable(0, EncodingUtils.newBinary(getVariables().size()));
		return solution;
	}

	@Override
	public void close() {
		// Nothing to do here?
	}

	@Override
	public String getName() {
		return "MOEA Problem";
	}
}
