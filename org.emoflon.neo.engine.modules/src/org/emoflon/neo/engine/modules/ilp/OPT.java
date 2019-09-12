package org.emoflon.neo.engine.modules.ilp;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.engine.ilp.BinaryILPProblem;
import org.emoflon.neo.engine.ilp.ILPProblem.Objective;

public class OPT {
	private Map<IMatch, Long> matchToId;
	private Map<Long, Set<IMatch>> elementToCreatingMatches;
	private Map<Long, Set<IMatch>> elementToDependentMatches;
	private Map<IMatch, Set<Long>> matchToCreatedElements;
	private Set<Set<IMatch>> cycles;

	private long constraintCounter;
	private long variableCounter;
	private Map<IMatch, Long> matchToWeight;
	private BinaryILPProblem ilpProblem;

	/**
	 * Creates the ILP Problem. Matches become binary variables to choose.
	 * Dependencies between matches are encoded as constraints
	 * 
	 * @return the ILP Problem
	 */
	public OPT(Collection<IMatch> matches) {
		// Precedence information
		registerMatches();
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
	
	protected void registerMatches() {
		//TODO[Anjorin] fill precedence tables with information from matches
		matchToId = new HashMap<>();
		elementToCreatingMatches = new HashMap<>();
		elementToDependentMatches = new HashMap<>();
		matchToCreatedElements = new HashMap<>();
	}
	
	protected void computeWeights() {
		//TODO[Anjorin] Compute weights for matches (matchToWeight)
		// - Number of "green" elements in the match
		matchToWeight = new HashMap<>();
	}

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
		
		for (var match : matchToId.keySet())
			computeCycles(match, Set.of());
		
		for (var cycle : cycles) {
			ilpProblem.addExclusion(cycle.stream().map(this::varNameFor), registerConstraint("EXCL_cycle"),
					cycle.size() - 1);
		}
	}

	private void computeCycles(IMatch match, Set<IMatch> cycle) {
		for(var element : matchToCreatedElements.get(match)) {
			for(var child : elementToCreatingMatches.get(element)) {
				if(cycle.contains(child))
					cycles.add(cycle);
				else {	
					var newCycle = new HashSet<>(cycle);
					newCycle.add(child);
					computeCycles(child, newCycle);
				}
			}
		}
	}
	
	protected void defineILPObjective() {
		var expr = ilpProblem.createLinearExpression();
		matchToWeight.keySet().stream().forEach(v -> {
			double weight = this.matchToWeight.get(v);
			expr.addTerm("x" + v, weight);
		});
		ilpProblem.setObjective(expr, Objective.maximize);
	}
	
	private String varNameFor(IMatch m) {
		if (!matchToId.containsKey(m))
			matchToId.put(m, variableCounter++);

		return "x" + matchToId.get(m);
	}

	private String registerConstraint(String label) {
		return label + "_" + constraintCounter++;
	}
}
