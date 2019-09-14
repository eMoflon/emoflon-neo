package org.emoflon.neo.engine.modules.ilp;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.emoflon.neo.engine.api.rules.ICoMatch;
import org.emoflon.neo.engine.ilp.BinaryILPProblem;
import org.emoflon.neo.engine.ilp.ILPProblem.Objective;

public abstract class OPT {
	protected Map<ICoMatch, Long> matchToId;
	private Map<Long, Set<ICoMatch>> elementToCreatingMatches;
	private Map<Long, Set<ICoMatch>> elementToDependentMatches;
	private Map<ICoMatch, Set<Long>> matchToCreatedElements;
	private Set<Set<ICoMatch>> cycles;
	private Set<ICoMatch> visited;
	private Set<ICoMatch> visiting;

	private long constraintCounter;
	private long variableCounter;
	protected Map<ICoMatch, Long> matchToWeight;
	private BinaryILPProblem ilpProblem;

	/**
	 * Creates the ILP Problem. Matches become binary variables to choose.
	 * Dependencies between matches are encoded as constraints
	 * 
	 * @return the ILP Problem
	 */
	public OPT(Collection<ICoMatch> matches) {
		// Precedence information
		registerMatches(matches);
		computeWeights();

		// ILP definition
		constraintCounter = 0;
		variableCounter = 0;
		ilpProblem = ILPFactory.createBinaryILPProblem();

		defineILPExclusions();
		defineILPImplications();
		defineILPObjective();
	}

	public BinaryILPProblem getILPProblem() {
		return ilpProblem;
	}

	// TODO[Anjorin] fill precedence tables with information from matches
	protected void registerMatches(Collection<ICoMatch> matches) {
		matchToId = new HashMap<>();
		elementToCreatingMatches = new HashMap<>();
		elementToDependentMatches = new HashMap<>();
		matchToCreatedElements = new HashMap<>();
		
		long i = 0;
		for (var m : matches) {
			matchToId.put(m, i++);
		}
	}

	abstract protected void computeWeights();

	protected void defineILPImplications() {
		for (var entry : elementToCreatingMatches.entrySet()) {
			var children = entry.getValue();
			var parents = elementToDependentMatches.getOrDefault(entry.getKey(), Collections.emptySet());

			if (!parents.isEmpty()) {
				// If no parent is chosen, no child can be chosen
				ilpProblem.addNegativeImplication(parents.stream().map(this::varNameFor),
						children.stream().map(this::varNameFor), registerConstraint("IMPL_" + entry.getKey()));
			} else {
				// There is no match creating this element -> forbid all matches needing it
				children.stream().forEach(m -> ilpProblem.fixVariable(varNameFor(m), false));
			}
		}
	}

	protected void defineILPExclusions() {
		for (var entry : elementToDependentMatches.entrySet()) {
			var parents = entry.getValue();
			if (parents.size() > 1) {
				// A child can only have one parent that marks it
				ilpProblem.addExclusion(parents.stream().map(this::varNameFor),
						registerConstraint("EXCL_MarkOnce_" + entry.getKey()));
			}
		}

		cycles = new HashSet<>();
		visited = new HashSet<>();
		visiting = new HashSet<>();
		for (var match : matchToId.keySet())
			computeCycles(match, new HashSet<>());

		for (var cycle : cycles) {
			ilpProblem.addExclusion(cycle.stream().map(this::varNameFor), registerConstraint("EXCL_cycle"),
					cycle.size() - 1);
		}
	}

	private void computeCycles(ICoMatch match, Set<ICoMatch> cycle) {
		if (visited.contains(match))
			return;

		if (visiting.contains(match)) {
			cycles.add(cycle);
			return;
		}

		cycle.add(match);
		visiting.add(match);
		for (var element : matchToCreatedElements.get(match)) {
			for (var child : elementToCreatingMatches.get(element)) {
				var newCycle = new HashSet<>(cycle);
				newCycle.add(child);
				computeCycles(child, newCycle);
			}
		}

		visited.add(match);
		visiting.remove(match);

	}

	protected void defineILPObjective() {
		var expr = ilpProblem.createLinearExpression();
		matchToWeight.keySet().stream().forEach(v -> {
			double weight = this.matchToWeight.get(v);
			expr.addTerm("x" + v, weight);
		});
		ilpProblem.setObjective(expr, Objective.maximize);
	}

	private String varNameFor(ICoMatch m) {
		if (!matchToId.containsKey(m))
			matchToId.put(m, variableCounter++);

		return "x" + matchToId.get(m);
	}

	private String registerConstraint(String label) {
		return label + "_" + constraintCounter++;
	}
}
