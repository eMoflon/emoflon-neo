package org.emoflon.neo.engine.modules.ilp;

import org.emoflon.neo.engine.ilp.BinaryILPProblem;
import org.emoflon.neo.engine.ilp.ILPProblem;
import org.emoflon.neo.engine.ilp.ILPSolver;
import org.emoflon.neo.engine.ilp.MOEAProblem;

/**
 * This static class offers methods to create ILP solvers and ILP problems
 *
 * @author Robin Oppermann
 */
public final class ILPFactory {
	
	public static final SupportedILPSolver exactSolver = SupportedILPSolver.Gurobi;
	public static final SupportedILPSolver heuristicSolver = SupportedILPSolver.MOEA;
	private static final int maxEvaluationsHeuristic  = 1000000;
	
	/**
	 * Private constructor. Should never be used as this class should only be used
	 * in a static context
	 */
	private ILPFactory() {
		throw new UnsupportedOperationException("You cannot instantiate this factory class");
	}

	/**
	 * Creates an ILP Solver for a pseudo-boolean problem. Variable solutions are
	 * restricted to (0,1).
	 *
	 * @param solver
	 *            Specifies the solver to use
	 *            supported.
	 * @return The created solver
	 */
	public static ILPSolver createBinaryILPSolver(final ILPProblem ilpProblem, final SupportedILPSolver solver) {
		switch (solver) {
		case Gurobi:
			return new GurobiWrapper(ilpProblem, true);
		case Sat4J:
			return new Sat4JWrapper(ilpProblem);
		case MOEA:
			return new MOEAWrapper(ilpProblem, maxEvaluationsHeuristic);
		case Google_OR:
			return new GoogleORWrapper(ilpProblem);
		default:
			throw new UnsupportedOperationException("Unknown Solver: " + solver.toString());
		}
	}

	/**
	 * Creates an ILP Solver.
	 *
	 * @param solver
	 *            Specifies the solver to use.
	 * @return The created solver.
	 */
	public static ILPSolver createILPSolver(final ILPProblem ilpProblem, final SupportedILPSolver solver) {
		if (ilpProblem instanceof BinaryILPProblem)
			return ILPFactory.createBinaryILPSolver(ilpProblem, solver);

		switch (solver) {
		case Gurobi:
			return new GurobiWrapper(ilpProblem, false);
		case Sat4J:
			throw new UnsupportedOperationException("SAT4J does not support arbitrary ILP");
		case MOEA:
			throw new UnsupportedOperationException("MOEA does not support arbitrary ILP");
		case Google_OR:
			throw new UnsupportedOperationException("TODO");
		default:
			throw new UnsupportedOperationException("Unknown Solver: " + solver.toString());
		}
	}

	/**
	 * Creates a new empty ILPProblem
	 *
	 * @return the created ILPProblem which can be filled with constraints and
	 *         objecitves afterwards
	 */
	public static ILPProblem createILPProblem() {
		return new ILPProblem();
	}

	/**
	 * Creates a {@link BinaryILPProblem}, in which variables can only be 0 or 1
	 *
	 * @return The created problem
	 */
	public static BinaryILPProblem createBinaryILPProblem(SupportedILPSolver solver) {
		switch (solver) {
		case Gurobi:
			return new BinaryILPProblem();
		case Sat4J:
			return new BinaryILPProblem();
		case MOEA:
			return new MOEAProblem();
		case Google_OR:
			return new BinaryILPProblem();
		default:
			throw new UnsupportedOperationException("Unknown Solver: " + solver.toString());
		}
	}

	/**
	 * This enum contains identifiers of the supported ILP Solvers.
	 *
	 * @author Robin Oppermann
	 *
	 */
	public enum SupportedILPSolver {
		/**
		 * Use the Gurobi solver (must be manually installed)
		 */
		Gurobi,
		/**
		 * Use the SAT4J solver (distributed with eclipse)
		 */
		Sat4J,
		/**
		 * Not an ILP solver, but some external framework for evolutionary algorithms
		 */
		MOEA,
		/**
		 * Use Google OR tools
		 */
		Google_OR;
		
		public String toString() {
			return this.toString();
		}
	}
}
