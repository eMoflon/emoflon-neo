package org.emoflon.neo.engine.modules.ilp;

import java.util.HashMap;
import java.util.Map;

import org.emoflon.neo.engine.ilp.ILPProblem;
import org.emoflon.neo.engine.ilp.ILPProblem.ILPConstraint;
import org.emoflon.neo.engine.ilp.ILPProblem.ILPObjective;
import org.emoflon.neo.engine.ilp.ILPProblem.ILPSolution;
import org.emoflon.neo.engine.ilp.ILPProblem.Objective;
import org.emoflon.neo.engine.ilp.ILPSolver;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

final class GoogleORWrapper extends ILPSolver {

	private MPSolver solver;
	private Map<Integer, MPVariable> id2var;
	private MPObjective objectiveFunc;

	protected GoogleORWrapper(ILPProblem ilpProblem) {
		super(ilpProblem);
		id2var = new HashMap<>();
		Loader.loadNativeLibraries();
	}

	@Override
	public ILPSolution solveILP() throws Exception {
		// Create the linear solver with the GLOP backend.
		solver = MPSolver.createSolver("CP_SAT");
		if (solver == null) {
			throw new IllegalStateException("Could not create solver GLOP");
		}

		for (int variableId : this.ilpProblem.getVariableIdsOfUnfixedVariables()) {
			this.registerVariable(variableId);
		}
		for (ILPConstraint constraint : this.ilpProblem.getConstraints()) {
			this.registerConstraint(constraint);
		}
		this.registerObjective(this.ilpProblem.getObjective());

		return this.retrieveSolution();
	}

	private ILPSolution retrieveSolution() {
		final MPSolver.ResultStatus resultStatus = solver.solve();

		boolean optimal = resultStatus == MPSolver.ResultStatus.OPTIMAL;

		if (resultStatus == MPSolver.ResultStatus.INFEASIBLE) {
			ILPSolver.logger.error("No optimal or feasible solution found.");
			throw new RuntimeException("No optimal or feasible solution found.");
		}

		double optimum = objectiveFunc.value();

		Map<Integer, Integer> solutionVariables = new HashMap<>();
		
		for (int variableId : this.ilpProblem.getVariableIdsOfUnfixedVariables()) {
			MPVariable var = this.id2var.get(variableId);
//			ILPSolver.logger.info(var.name() + "=>" +  var.solutionValue());
			solutionVariables.put(variableId, (int) Math.rint(var.solutionValue()));
		}

//		ILPSolver.logger.info("Google OR found solution: " + optimum + " - Optimal: " + optimal);

		ILPSolution solution = this.ilpProblem.createILPSolution(solutionVariables, optimal, optimum);
//		ILPSolver.logger.info(solutionVariables);
//		ILPSolver.logger.info(solution.getSolutionInformation());
//		ILPSolver.logger.info(solver.exportModelAsLpFormat());
//		ILPSolver.logger.info(solution);
		return solution;
	}

	private void registerObjective(ILPObjective objective) {
		objectiveFunc = solver.objective();

		objective.getLinearExpression().getVariables().forEach(varId -> {
			objectiveFunc.setCoefficient(id2var.get(varId), objective.getLinearExpression().getCoefficient(varId));
		});

		if (objective.getObjectiveOperation() == Objective.maximize)
			objectiveFunc.setMaximization();
		else
			objectiveFunc.setMinimization();
	}

	private void registerConstraint(ILPConstraint constraint) {
		double infinity = MPSolver.infinity();

		// Create a linear constraint: lhs comparator rhs
		double upperBound;
		double lowerBound;
		switch (constraint.getComparator()) {
		case eq:
			upperBound = constraint.getValue();
			lowerBound = constraint.getValue();
			break;
		case ge:
		case gt:
			lowerBound = constraint.getValue();
			upperBound = infinity;
			break;
		case le:
		case lt:
			upperBound = constraint.getValue();
			lowerBound = -infinity;
			break;
		default:
			throw new IllegalArgumentException("Unexpected value: " + constraint.getComparator());
		}

		// Set coefficients
		MPConstraint ct = solver.makeConstraint(lowerBound, upperBound, constraint.getName());
		constraint.getLinearExpression().getVariables().forEach(varId -> {
			ct.setCoefficient(this.id2var.get(varId), constraint.getLinearExpression().getCoefficient(varId));
		});
	}

	private void registerVariable(int variableId) {
		String variable = this.ilpProblem.getVariable(variableId);
		MPVariable var = solver.makeIntVar(0.0, 1.0, variable);
		this.id2var.put(variableId, var);
	}

}
