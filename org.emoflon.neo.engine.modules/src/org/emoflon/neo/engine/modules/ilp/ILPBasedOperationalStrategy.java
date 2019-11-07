package org.emoflon.neo.engine.modules.ilp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.emoflon.neo.cypher.constraints.NeoNegativeConstraint;
import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.cypher.rules.NeoRule;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IUpdatePolicy;
import org.emoflon.neo.engine.ilp.BinaryILPProblem;
import org.emoflon.neo.engine.ilp.ILPProblem.Objective;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;

public abstract class ILPBasedOperationalStrategy implements IUpdatePolicy<NeoMatch, NeoCoMatch> {
	private static final Logger logger = Logger.getLogger(ILPBasedOperationalStrategy.class);

	protected Map<IMatch, String> matchToId;
	protected Map<Long, Set<IMatch>> elementToCreatingMatches;
	protected Map<Long, Set<IMatch>> elementToDependentMatches;
	protected Map<IMatch, Set<Long>> matchToCreatedElements;
	protected Set<Set<IMatch>> cycles;
	protected Set<IMatch> visited;
	protected Set<IMatch> visiting;

	private int constraintCounter;
	private int variableCounter;
	private int auxVariableCounter;
	protected Map<IMatch, Integer> matchToWeight;
	private BinaryILPProblem ilpProblem;

	protected Map<String, IRule<NeoMatch, NeoCoMatch>> genRules;
	protected Collection<NeoNegativeConstraint> negativeConstraints;

	public ILPBasedOperationalStrategy(Collection<NeoRule> genRules, Collection<IConstraint> negativeConstraints) {
		matchToId = new HashMap<>();
		matchToCreatedElements = new HashMap<>();
		elementToCreatingMatches = new HashMap<>();
		elementToDependentMatches = new HashMap<>();

		this.genRules = new HashMap<>();
		genRules.forEach(tr -> this.genRules.put(tr.getName(), tr));

		this.negativeConstraints = new ArrayList<>();
		negativeConstraints.forEach(nc -> {
			if (nc instanceof NeoNegativeConstraint) {
				this.negativeConstraints.add((NeoNegativeConstraint) nc);
			} else {
				throw new IllegalArgumentException(
						"Only negative domain constraints are supported at the moment: " + nc);
			}
		});
	}

	protected void computeILPProblem() {
		// ILP definition
		constraintCounter = 0;
		variableCounter = 0;
		auxVariableCounter = 0;
		ilpProblem = ILPFactory.createBinaryILPProblem();

		logger.debug("Defining exclusions...");
		defineILPExclusions();

		logger.debug("Defining implications...");
		defineILPImplications();

		logger.debug("Defining objective...");
		defineILPObjective();

		logger.debug("Handling constraint violations...");
		handleConstraintViolations();

		logger.debug("Created ILP problem.");
	}

	protected void registerMatches(Stream<? extends IMatch> matches) {
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
	public Map<IRule<NeoMatch, NeoCoMatch>, Collection<NeoMatch>> selectMatches(
			MatchContainer<NeoMatch, NeoCoMatch> matches, IMonitor<NeoMatch, NeoCoMatch> progressMonitor) {
		logger.debug("Registering all matches...");

		// Precedence information
		registerMatches(matches.streamAllMatches());
		computeWeights();

		logger.debug("Registered all matches.");

		return Collections.emptyMap();
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
			}

			if (creatingMatches.isEmpty()) {
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
			ilpProblem.addExclusion(cycle.stream().map(this::varNameFor), registerConstraint("EXCL_Cycle"),
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
		if (!matchToId.containsKey(m)) {
			throw new IllegalArgumentException(
					"You're requesting a variable name for an id that hasn't been registered yet: " + m.getPattern());
		}

		return matchToId.get(m);
	}

	private String varName(long id) {
		return "x" + id;
	}

	private String registerConstraint(String label) {
		return label + "_" + constraintCounter++;
	}

	protected void handleConstraintViolations() {
		long tic = System.currentTimeMillis();
		logger.info("Checking for constraint violations...");
		var violations = negativeConstraints.stream().flatMap(nc -> nc.getViolations().stream());
		logger.info("Completed in " + (System.currentTimeMillis() - tic) / 1000.0 + "s");

		violations.forEach(v -> {
			var elements = extractIDs(v.getPattern().getElements(), v);

			var auxVariables = new ArrayList<String>();
			var elementsThatCanNeverBeMarked = new ArrayList<Long>();
			elements.forEach(elt -> {
				var creatingMatches = elementToCreatingMatches.getOrDefault(elt, Collections.emptySet());

				if (!creatingMatches.isEmpty()) {
					var auxVarForElt = "aux" + auxVariableCounter++;
					auxVariables.add(auxVarForElt);

					// one of the matches that create elt is chosen ==> aux variable must be chosen
					// below a contraposition of this implication is used
					ilpProblem.addNegativeImplication(Stream.of(auxVarForElt),
							creatingMatches.stream().map(this::varNameFor),
							registerConstraint("AUX_" + v.getPattern().getName() + "_" + elt));
				} else {
					elementsThatCanNeverBeMarked.add(elt);
				}
			});

			// If the violation requires an element that can never be marked no exclusion
			// constraint is necessary
			if (elementsThatCanNeverBeMarked.isEmpty()) {
				// If all aux variables are chosen then the negative constraint
				// is violated so forbid this
				ilpProblem.addExclusion(auxVariables.stream(), "EXCL_NEG_CONSTR_" + v.getPattern().getName(),
						auxVariables.size() - 1);
			}
		});
	}

	public Optional<Set<Long>> determineConsistentElements(SupportedILPSolver suppSolver) throws Exception {
		computeILPProblem();
		var solver = ILPFactory.createILPSolver(ilpProblem, suppSolver);

		logger.debug(ilpProblem);

		var solution = solver.solveILP();

		logger.debug(solution);

		if (solution.isOptimal() || auxVariableCounter == 0)
			return Optional.of(matchToId.entrySet().stream()//
					.filter(entry -> solution.getVariable(entry.getValue()) > 0)
					.flatMap(entry -> getCreatedElts(entry.getKey()).stream()).collect(Collectors.toSet()));
		else
			return Optional.empty();
	}

	public Optional<Set<Long>> determineInconsistentElements(SupportedILPSolver suppSolver) throws Exception {
		var consistentElements = determineConsistentElements(suppSolver);

		if (consistentElements.isPresent()) {
			var allElements = new HashSet<>(elementToCreatingMatches.keySet());
			allElements.removeAll(consistentElements.get());
			return Optional.of(allElements);
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Get ids from a match. Note that edge and node ids are orthogonal. To avoid
	 * duplicate ids, edge ids are prepended with a - to retain uniqueness.
	 * 
	 * @param elements
	 * @param m
	 * @return
	 */
	protected Set<Long> extractIDs(Collection<String> elements, IMatch m) {
		return elements.stream()//
				.filter(name -> m.getNodeIDs().containsKey(name) || m.getEdgeIDs().containsKey(name))//
				.map(name -> m.getNodeIDs().containsKey(name) ? //
						m.getNodeIDs().get(name) : -1 * m.getEdgeIDs().get(name))//
				.collect(Collectors.toSet());
	}

	public int getNrOfILPConstraints() {
		return ilpProblem.getConstraints().size();
	}

	public int getNroOfGraphConstraints() {
		return negativeConstraints.size();
	}

	public String getInfo() {
		return ilpProblem.getProblemInformation();
	}
}
