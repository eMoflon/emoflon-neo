package org.emoflon.neo.emsl.compiler.attributeConstraints.sorting;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.emoflon.neo.emsl.compiler.Operation;
import org.emoflon.neo.emsl.compiler.ParameterData;
import org.emoflon.neo.emsl.compiler.TGGCompiler;
import org.emoflon.neo.emsl.compiler.attributeConstraints.sorting.solver.democles.common.Adornment;
import org.emoflon.neo.emsl.compiler.attributeConstraints.sorting.solver.democles.plan.Algorithm;
import org.emoflon.neo.emsl.compiler.attributeConstraints.sorting.solver.democles.plan.WeightedOperation;
import org.emoflon.neo.emsl.compiler.ops.MODELGEN;
import org.emoflon.neo.emsl.eMSL.AttributeConstraint;
import org.emoflon.neo.emsl.eMSL.ConstraintArgValue;
import org.emoflon.neo.emsl.eMSL.Parameter;
import org.emoflon.neo.emsl.eMSL.ValueExpression;

import com.google.common.collect.Lists;

public class SearchPlanAction extends Algorithm<SimpleCombiner, AttributeConstraint> {

	private final List<InternalVariable> variables;
	private final List<AttributeConstraint> constraints = new ArrayList<>();
	private final Operation op;
	private final Map<Parameter, ParameterData> paramsToData;

	/**
	 * @param variables
	 * @param constraints
	 * @param useGenAdornments
	 * @param availableNodes   Nodes from which bound values for parameters are
	 *                         taken.
	 */
	public SearchPlanAction(List<ValueExpression> variables, List<AttributeConstraint> constraints,
			Operation op, Map<Parameter, ParameterData> paramsToData) {
		this.variables = variables.stream()//
				.map(v -> new InternalVariable(v, TGGCompiler.handleValue(v, paramsToData)))//
				.distinct()//
				.collect(Collectors.toList());
		this.constraints.addAll(constraints);
		this.op = op;
		this.paramsToData = paramsToData;
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
					op.getClass().getSimpleName() + ": " +
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
			if (isBoundInPattern(variables.get(i).getValue())) {
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
	public boolean isBoundInPattern(ValueExpression value) {
		var operationalisedValue = TGGCompiler.handleValue(value, paramsToData);
		return !(operationalisedValue.startsWith("<") && operationalisedValue.endsWith(">"));
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
		if (op instanceof MODELGEN) {
			var adornments = new ArrayList<String>(constraint.getType().getAdornments());
			adornments.addAll(constraint.getType().getGenAdornments());
			return adornments;
		} else
			return constraint.getType().getAdornments();
	}

	private WeightedOperation<AttributeConstraint> createWeightedOperationForConstraintWithAdornment(
			final AttributeConstraint constraint, final String adornment) {
		long frees = adornment.chars().filter(c -> c == 'F').count();
		float weight = (float) Math.pow(frees, 3);

		return createOperation(constraint, createBoundMask(constraint, adornment),
				createFreeMask(constraint, adornment), weight);
	}

	private Adornment createBoundMask(final AttributeConstraint constraint, final String adornment) {
		return createMask(constraint, adornment, 'B');
	}

	private Adornment createFreeMask(final AttributeConstraint constraint, final String adornment) {
		return createMask(constraint, adornment, 'F');
	}

	private Adornment createMask(final AttributeConstraint constraint, final String adornment, char mode) {
		boolean[] bits = new boolean[variables.size()];

		var adornmentChars = adornment.split(" ");
		assert (adornmentChars.length == constraint.getValues().size());

		for (int i = 0; i < constraint.getValues().size(); i++) {
			ConstraintArgValue arg = constraint.getValues().get(i);
			int index = variables.indexOf(new InternalVariable(arg.getValue(), TGGCompiler.handleValue(arg.getValue(), paramsToData)));
			if (adornmentChars[i].equals(String.valueOf(mode))) {
				bits[index] = true;
			}
		}
		return new Adornment(bits);
	}
}
