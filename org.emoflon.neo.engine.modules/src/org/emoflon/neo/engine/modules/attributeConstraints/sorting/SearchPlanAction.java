package org.emoflon.neo.engine.modules.attributeConstraints.sorting;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.emoflon.neo.emsl.eMSL.AttributeConstraint;
import org.emoflon.neo.emsl.eMSL.ConstraintArgValue;
import org.emoflon.neo.engine.modules.attributeConstraints.sorting.solver.democles.common.Adornment;
import org.emoflon.neo.engine.modules.attributeConstraints.sorting.solver.democles.plan.Algorithm;
import org.emoflon.neo.engine.modules.attributeConstraints.sorting.solver.democles.plan.WeightedOperation;

import com.google.common.collect.Lists;


public class SearchPlanAction extends Algorithm<SimpleCombiner, AttributeConstraint> {

	private final List<ConstraintArgValue> variables = new ArrayList<>();
	private final List<AttributeConstraint> constraints = new ArrayList<>();
	private final boolean useGenAdornments;

	/**
	 * @param variables
	 * @param constraints
	 * @param useGenAdornments
	 * @param availableNodes
	 *            Nodes from which bound values for parameters are taken.
	 */
	public SearchPlanAction(List<ConstraintArgValue> variables, List<AttributeConstraint> constraints,
			boolean useGenAdornments) {
		this.variables.addAll(variables);
		this.constraints.addAll(constraints);
		this.useGenAdornments = useGenAdornments;
	}

	// Unsorted list of our constraints => return a new List where constraints are
	// sorted according to the search plan
	public List<AttributeConstraint> sortConstraints() {
		if (constraints.isEmpty())
			return Collections.emptyList();

		// 1. Determine inputAdornment (initial binding information) from sorted (!)
		// list of variables
		Adornment inputAdornment = determineInputAdornment();

		// 2. Create weighted operations, one for each allowed adornment of each
		// constraint
		List<WeightedOperation<AttributeConstraint>> weightedOperations = createWeightedOperations(constraints);

		// 3. Call search plan algorithm to sort weighted operations

		PrintStream out = System.out;
		PrintStream err = System.err;
		System.setOut(new PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
			}
		}));
		System.setErr(new PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
			}
		}));

		try {
			SimpleCombiner sc = generatePlan(new SimpleCombiner(), weightedOperations, inputAdornment);
			List<AttributeConstraint> sortedList = new ArrayList<>();
			Chain<?> c = sc.getRoot();
			while (c != null) {
				sortedList.add((AttributeConstraint) c.getValue());
				c = c.getNext();
			}
			return Lists.reverse(sortedList);
		} catch (Exception e) {
			throw new IllegalStateException(
					constraints.stream().map(c -> c.getType().getName()).collect(Collectors.toList()) + ", "
							+ e.getMessage());
		} finally {
			System.setOut(out);
			System.setErr(err);
		}
	}

	private Adornment determineInputAdornment() {
		boolean[] bits = new boolean[variables.size()];
		for (int i = 0; i < variables.size(); i++) {
			if (isBoundInPattern(variables.get(i))) {
				bits[i] = Adornment.B; // Bound <-> false !
			} else {
				bits[i] = Adornment.F; // Unbound <-> true !
			}
		}
		Adornment inputAdornment = new Adornment(bits);
		return inputAdornment;
	}

	/**
	 * A variable is bound in a pattern if its value will be fixed when solving
	 * attribute conditions. This is the case for attribute expressions referencing
	 * black nodes (nodes in the pattern), never the case for local variables
	 * (attribute variables), and always the case for constants (literals and
	 * enums).
	 * 
	 * @param variable
	 * @param patternContainsNode
	 * @return
	 */
	public static boolean isBoundInPattern(ConstraintArgValue variable) {
		//FIXME:  Implement
		throw new IllegalStateException("Unable to handle " + variable);
	}
	
	/**
	 * Create weighted operations from constraints
	 * 
	 * @param constraints
	 * @return
	 */
	private List<WeightedOperation<AttributeConstraint>> createWeightedOperations(
			final List<AttributeConstraint> constraints) {
		List<WeightedOperation<AttributeConstraint>> result = new ArrayList<WeightedOperation<AttributeConstraint>>();
		// for each constraint ...
		for (AttributeConstraint constraint : constraints) {
			// and each allowed adornment ...
			for (String adornment : getAllowedAdornmentsForMode(constraint)) {
				result.add(createWeightedOperationForConstraintWithAdornment(constraint, adornment));
			}
		}
		return result;
	}

	private List<String> getAllowedAdornmentsForMode(AttributeConstraint constraint) {
		//FIXME:  Take useGenAdornments flat into account
		if (useGenAdornments)
			return constraint.getType().getAdornments();
		else
			return constraint.getType().getAdornments();
	}

	private WeightedOperation<AttributeConstraint> createWeightedOperationForConstraintWithAdornment(
			final AttributeConstraint constraint, final String adornment) {
		long frees = adornment.codePoints().filter(c -> c == 'F').count();
		float weight = (float) Math.pow(frees, 3);

		return createOperation(constraint, createBoundMask(constraint, adornment),
				createFreeMask(constraint, adornment), weight);
	}

	private Adornment createBoundMask(final AttributeConstraint constraint,
			final String adornment) {
		return createMask(constraint, adornment, 'B');
	}

	private Adornment createFreeMask(final AttributeConstraint constraint,
			final String adornment) {
		return createMask(constraint, adornment, 'F');
	}

	private Adornment createMask(final AttributeConstraint constraint,
			final String adornment, char mode) {
		boolean[] bits = new boolean[variables.size()];

		for (int i = 0; i < constraint.getValues().size(); i++) {
			ConstraintArgValue variable = constraint.getValues().get(i);
			int index = variables.indexOf(variable);
			if (adornment.toCharArray()[i] == mode) {
				bits[index] = true;
			}
		}
		return new Adornment(bits);
	}
}
