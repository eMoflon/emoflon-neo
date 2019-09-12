package org.emoflon.neo.engine.ilp;

import org.apache.log4j.Logger;
import org.emoflon.neo.engine.ilp.ILPProblem.ILPSolution;

/**
 * This class is used to abstract from the usage of a concrete ILP solver. It
 * provides common methods to define ILP problems, start finding the solution
 * and access to the found solution.
 * 
 * @author Robin Oppermann
 */
public abstract class ILPSolver {

	/**
	 * The problem to solve
	 */
	protected final ILPProblem ilpProblem;

	/**
	 * Logger for all ILPSolvers
	 */
	protected final static Logger logger = Logger.getLogger(ILPSolver.class);
	
	/**
	 * The tolerance between found solution and theoretical optimum at which the solver is allowed to stop
	 */
	private double solutionTolerance = 0;

	/**
	 * Creates an ILPSolver
	 * 
	 * @param ilpProblem
	 *            The {@link ILPProblem} to solve
	 */
	protected ILPSolver(ILPProblem ilpProblem) {
		this.ilpProblem = ilpProblem;
	}

	/**
	 * Starts solving the ILP
	 * 
	 * @return The solution of the ILP (if found)
	 * @throws Exception
	 */
	protected abstract ILPSolution solveILP() throws Exception;

	/**
	 * @return The tolerance between found solution and theoretical optimum at which the solver is allowed to stop
	 */
	public final double getSolutionTolerance() {
		return solutionTolerance;
	}

	/**
	 * @param solutionTolerance The tolerance between found solution and theoretical optimum at which the solver is allowed to stop
	 */
	public final void setSolutionTolerance(double solutionTolerance) {
		this.solutionTolerance = solutionTolerance;
	}
}