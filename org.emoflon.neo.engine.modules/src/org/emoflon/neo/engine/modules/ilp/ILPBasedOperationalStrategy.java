package org.emoflon.neo.engine.modules.ilp;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IUpdatePolicy;
import org.emoflon.neo.engine.ilp.BinaryILPProblem;
import org.emoflon.neo.engine.ilp.ILPProblem;
import org.emoflon.neo.engine.ilp.ILPProblem.Objective;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

public abstract class ILPBasedOperationalStrategy implements IUpdatePolicy<NeoMatch, NeoCoMatch> {
	protected Map<IMatch, String> matchToId;
	protected Map<Long, Set<IMatch>> elementToCreatingMatches;
	protected Map<Long, Set<IMatch>> elementToDependentMatches;
	protected Map<IMatch, Set<Long>> matchToCreatedElements;
	protected Set<Set<IMatch>> cycles;
	protected Set<IMatch> visited;
	protected Set<IMatch> visiting;

	private int constraintCounter;
	private int variableCounter;
	protected Map<IMatch, Integer> matchToWeight;
	private BinaryILPProblem ilpProblem;

	protected Map<String, IRule<NeoMatch, NeoCoMatch>> genRules;

	public ILPBasedOperationalStrategy(Collection<IRule<NeoMatch, NeoCoMatch>> genRules) {
		this.genRules = new HashMap<>();
		genRules.forEach(tr -> this.genRules.put(tr.getName(), tr));
	}

	public void computeILPProblem(Stream<IMatch> matches) {
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

	public ILPProblem getILPProblem() {
		return ilpProblem;
	}

	protected void registerMatches(Stream<IMatch> matches) {
		matchToId = new HashMap<>();
		matchToCreatedElements = new HashMap<>();
		elementToCreatingMatches = new HashMap<>();
		elementToDependentMatches = new HashMap<>();

		matches.forEach(m -> {
			matchToId.put(m, varName(variableCounter++));

			var createdElts = getCreatedElts(m);
			var contextElts = getContextElts(m);
			matchToCreatedElements.put(m, createdElts);
			createdElts.forEach(x -> addMatch(x, m, elementToCreatingMatches));
			contextElts.forEach(x -> addMatch(x, m, elementToDependentMatches));
		});
	}

	@Override
	public Collection<NeoMatch> selectMatches(MatchContainer<NeoMatch, NeoCoMatch> pMatches,
			IMonitor pProgressMonitor) {
		computeILPProblem(pMatches.stream().map(m -> (IMatch) m));

		return Collections.emptySet();
	}

	protected void computeWeights() {
		matchToWeight = new HashMap<>();
		for (var m : matchToId.keySet())
			matchToWeight.put(m, getCreatedElts(m).size());
	}

	private void addMatch(Long x, IMatch m, Map<Long, Set<IMatch>> matches) {
		if (!matches.containsKey(x))
			matches.put(x, new HashSet<>());

		matches.get(x).add(m);
	}

	protected abstract Set<Long> getContextElts(IMatch m);

	protected abstract Set<Long> getCreatedElts(IMatch m);

	protected void defineILPImplications() {
		for (var entry : elementToCreatingMatches.entrySet()) {
			var creatingMatches = entry.getValue();
			var dependentMatches = elementToDependentMatches.getOrDefault(entry.getKey(), Collections.emptySet());

			if (!creatingMatches.isEmpty() && !dependentMatches.isEmpty()) {
				// If no creator is chosen, no dependent can be chosen
				ilpProblem.addNegativeImplication(creatingMatches.stream().map(this::varNameFor),
						dependentMatches.stream().map(this::varNameFor), registerConstraint("IMPL_" + entry.getKey()));
			} else {
				// There is no match creating this element -> forbid all matches needing it
				dependentMatches.stream().forEach(m -> ilpProblem.fixVariable(varNameFor(m), false));
			}
		}
	}

	protected void defineILPExclusions() {
		for (var entry : elementToCreatingMatches.entrySet()) {
			var creatingMatches = entry.getValue();
			if (creatingMatches.size() > 1) {
				// A child can only have one parent that marks it
				ilpProblem.addExclusion(creatingMatches.stream().map(this::varNameFor),
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

	private void computeCycles(IMatch match, Set<IMatch> cycle) {
		if (visited.contains(match))
			return;

		if (visiting.contains(match)) {
			cycles.add(cycle);
			return;
		}

		cycle.add(match);
		visiting.add(match);
		for (var element : matchToCreatedElements.get(match)) {
			for (var dependentMatch : elementToDependentMatches.getOrDefault(element, Collections.emptySet())) {
				var newCycle = new HashSet<>(cycle);
				newCycle.add(dependentMatch);
				computeCycles(dependentMatch, newCycle);
			}
		}

		visited.add(match);
		visiting.remove(match);
	}

	protected void defineILPObjective() {
		var expr = ilpProblem.createLinearExpression();
		matchToId.forEach((m, id) -> {
			double weight = matchToWeight.get(m);
			expr.addTerm(id, weight);
		});
		ilpProblem.setObjective(expr, Objective.maximize);
	}

	private String varNameFor(IMatch m) {
		return matchToId.get(m);
	}

	private String varName(long id) {
		return "x" + id;
	}

	private String registerConstraint(String label) {
		return label + "_" + constraintCounter++;
	}
}
