package org.emoflon.neo.engine.ilp;

import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import java.util.Collection;
import java.util.HashSet;

public class MOEAProblem extends BinaryILPProblem implements Problem {

	private Collection<ILPObjective> objectives = new HashSet<>();
	
	@Override
	public void evaluate(Solution solution) {
		
		boolean[] x = EncodingUtils.getBinary(solution.getVariable(0));
		
		// Objective Function
		int i = 0;
		
		for (ILPObjective o : objectives) {
			double ofv = 0;
			ILPLinearExpression le = o.getLinearExpression();
			
			for (String v : getVariables()) {
				ofv += le.getCoefficient(getVariableId(v)) * (x[getVariableId(v)-1] ? 1 : 0);
			}
			
			if (o.getObjectiveOperation().equals(Objective.minimize))
				solution.setObjective(i++, ofv);
			else if (o.getObjectiveOperation().equals(Objective.maximize))
				solution.setObjective(i++, -ofv);
			else 
				throw new IllegalArgumentException("Unsupported objective: " + o.getObjectiveOperation().toString());
		}
		
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
		Solution solution = new Solution(getNumberOfVariables(),objectives.size(),getNumberOfConstraints());
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
	
	public Collection<ILPObjective> getObjectives() {
		return objectives;
	}
	
	public void addObjective(ILPObjective o) {
		objectives.add(o);
		objective = o; // Intended for single objective optimization, needs to be improved
	}
}
