package org.emoflon.neo.engine.modules.ilp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.emoflon.neo.cypher.constraints.NeoConstraint;
import org.emoflon.neo.cypher.constraints.NeoNegativeConstraint;
import org.emoflon.neo.cypher.constraints.NeoPositiveConstraint;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.cypher.patterns.NeoBasicPattern;
import org.emoflon.neo.cypher.patterns.NeoImplicationPattern;
import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.cypher.rules.NeoRule;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.modules.IUpdatePolicy;
import org.emoflon.neo.engine.ilp.BinaryILPProblem;
import org.emoflon.neo.engine.ilp.ILPProblem.Objective;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;

public abstract class ILPBasedOperationalStrategy implements IUpdatePolicy<NeoMatch, NeoCoMatch> {
	private static final Logger logger = Logger.getLogger(ILPBasedOperationalStrategy.class);

	protected NeoCoreBuilder builder;

	protected Collection<Long> consistentElements;
	protected Collection<Long> inconsistentElements;

	protected Map<NeoMatch, String> matchToId;
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
	protected Map<String, IRule<NeoMatch, NeoCoMatch>> opRules;
	protected Collection<NeoNegativeConstraint> negativeConstraints;
	protected Collection<NeoPositiveConstraint> positiveConstraints;

	protected String sourceModel;
	protected String targetModel;

	protected SupportedILPSolver solver;

	public ILPBasedOperationalStrategy(//
			SupportedILPSolver solver, //
			Collection<NeoRule> genRules, //
			Collection<NeoRule> opRules, //
			Collection<IConstraint> negativeConstraints, //
			NeoCoreBuilder builder, //
			String sourceModel, //
			String targetModel//
	) {
		this.sourceModel = sourceModel;
		this.targetModel = targetModel;
		this.builder = builder;

		matchToId = new HashMap<>();
		matchToCreatedElements = new HashMap<>();
		elementToCreatingMatches = new HashMap<>();
		elementToDependentMatches = new HashMap<>();

		this.solver = solver;

		this.genRules = new HashMap<>();
		genRules.forEach(tr -> this.genRules.put(tr.getName(), tr));
		this.opRules = new HashMap<>();
		opRules.forEach(tr -> this.opRules.put(tr.getName(), tr));

		this.negativeConstraints = new ArrayList<>();
		this.positiveConstraints = new ArrayList<>();
		negativeConstraints.forEach(c -> {
			if (c instanceof NeoNegativeConstraint) {
				this.negativeConstraints.add((NeoNegativeConstraint) c);
			} else if (c instanceof NeoPositiveConstraint){
				this.positiveConstraints.add((NeoPositiveConstraint) c);
			} else {
				throw new IllegalArgumentException(
						"Only negative constraints and if/else constraints are supported at the moment: " + c);
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
		
		logger.debug("Handling positive constraints...");
		handlePositiveConstraints();

		logger.debug("Created ILP problem.");
	}

	protected void registerMatches(Stream<? extends NeoMatch> matches) {
		matches.forEach(m -> {
			matchToId.put(m, varName(variableCounter++));

			var createdElts = getCreatedAndMarkedElts(m);
			var contextElts = getContextElts(m);
			matchToCreatedElements.put(m, createdElts);
			createdElts.forEach(x -> addMatch(x, m, elementToCreatingMatches));
			contextElts.forEach(x -> addMatch(x, m, elementToDependentMatches));
		});
	}

	protected void computeWeights() {
		matchToWeight = new HashMap<>();
		for (var m : matchToId.keySet())
			matchToWeight.put(m, getMarkedElts(m).size());
	}

	private void addMatch(Long x, IMatch m, Map<Long, Set<IMatch>> matches) {
		if (!matches.containsKey(x))
			matches.put(x, new HashSet<>());

		matches.get(x).add(m);
	}

	protected Set<Long> getContextElts(NeoMatch m) {
		var genRule = genRules.get(m.getPattern().getName());
		var ids = extractNodeIDs(genRule.getContextNodeLabels(), m);
		ids.addAll(extractRelIDs(genRule.getContextRelLabels(), m));
		return ids;
	}

	protected Set<Long> getCreatedAndMarkedElts(NeoMatch m) {
		var genRule = genRules.get(m.getPattern().getName());
		var ids = extractNodeIDs(genRule.getCreatedNodeLabels(), m);
		ids.addAll(extractRelIDs(genRule.getCreatedRelLabels(), m));
		return ids;
	}

	protected Set<Long> getCreatedElts(NeoMatch m) {
		var ids = extractNodeIDs(opRules.get(m.getPattern().getName()).getCreatedNodeLabels(), m);
		ids.addAll(extractRelIDs(opRules.get(m.getPattern().getName()).getCreatedRelLabels(), m));
		return ids;
	}

	protected Set<Long> getMarkedElts(NeoMatch m) {
		var marked = getCreatedAndMarkedElts(m);
		var created = getCreatedElts(m);
		marked.removeAll(created);
		return marked;
	}

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
			var elements = extractNodeIDs(v.getPattern().getContextNodeLabels(), v);
			elements.addAll(extractRelIDs(v.getPattern().getContextRelLabels(), v));

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

	// Positive constraint - edit Surbhi
	protected void handlePositiveConstraints() {
		long tic = System.currentTimeMillis();
		logger.info("Checking for positive constraint...");
		var premise = positiveConstraints.stream().flatMap(pr -> pr.getPremise().stream());
		var conclusion = positiveConstraints.stream().flatMap(pr -> pr.getConclusion().stream());
		logger.info("Completed in " + (System.currentTimeMillis() - tic) / 1000.0 + "s");
		premise.forEach(p-> {
			var elements = extractNodeIDs(p.getPattern().getContextNodeLabels(), p);
			elements.addAll(extractRelIDs(p.getPattern().getContextRelLabels(), p));

			var auxVariablesP = new ArrayList<String>();
			var elementsThatCanNeverBeMarked = new ArrayList<Long>();
			
			elements.forEach(elt -> {
				var creatingMatches = elementToCreatingMatches.getOrDefault(elt, Collections.emptySet());

				if (!creatingMatches.isEmpty()) {
					//d2=>aux2
					var auxVarForEltP = "aux" + auxVariableCounter++;
					auxVariablesP.add(auxVarForEltP);

					//!aux2=>!d2
					ilpProblem.addNegativeImplication(Stream.of(auxVarForEltP),
							creatingMatches.stream().map(this::varNameFor),
							registerConstraint("AUX_" + p.getPattern().getName() + "_" + elt));
					
					//--------------------------------------------
					conclusion.forEach(c-> {
						var elements1 = extractNodeIDs(c.getPattern().getContextNodeLabels(), c);
						elements1.addAll(extractRelIDs(c.getPattern().getContextRelLabels(), c));

						var auxVariablesC = new ArrayList<String>();
						var elementsThatCanNeverBeMarkedCC = new ArrayList<Long>();
						
						elements1.forEach(elt1 -> {
							var creatingMatches1 = elementToCreatingMatches.getOrDefault(elt1, Collections.emptySet());

							if (!creatingMatches1.isEmpty()) {
								var auxVarForEltC = "aux" + auxVariableCounter++;
								auxVariablesC.add(auxVarForEltC);

								ilpProblem.addImplication(
										Stream.of(auxVarForEltP),
										Stream.of(auxVarForEltC),
										registerConstraint("AUX_" + c.getPattern().getName() + "_" + elt1 + "AUX_" + p.getPattern().getName() + "_" + elt));
							} else {
								elementsThatCanNeverBeMarkedCC.add(elt1);
							}
						});
					});
					
					//------------------------------------------------
				} else {
					elementsThatCanNeverBeMarked.add(elt);
				}
			});
		});
		
}

	public Collection<Long> determineConsistentElements() throws Exception {
		if (consistentElements == null) {
			computeILPProblem();
			var ilpSolver = ILPFactory.createILPSolver(ilpProblem, solver);

			logger.debug(ilpProblem);

			var solution = ilpSolver.solveILP();

			logger.debug(solution);

			if (solution.isOptimal() || auxVariableCounter == 0)
				consistentElements = matchToId.entrySet().stream()//
						.filter(entry -> solution.getVariable(entry.getValue()) > 0)
						.flatMap(entry -> getCreatedAndMarkedElts(entry.getKey()).stream())
						.collect(Collectors.toList());
			else
				throw new IllegalStateException("There should always be an optimal (= consistent) solution!");
		}

		return consistentElements;
	}

	public Collection<Long> determineInconsistentElements() throws Exception {
		if (inconsistentElements == null) {
			var consistentElements = determineConsistentElements();
			var allElements = builder.getAllElementIDsInTriple(sourceModel, targetModel);
			allElements.removeAll(consistentElements);
			inconsistentElements = allElements;
		}

		return inconsistentElements;
	}

	/**
	 * Get ids from a match. Note that edge and node ids are orthogonal. To avoid
	 * duplicate ids, edge ids are prepended with a - to retain uniqueness.
	 * 
	 * @param elements
	 * @param m
	 * @return
	 */
	protected Set<Long> extractNodeIDs(Collection<String> nodes, NeoMatch m) {
		return nodes.stream()//
				.filter(name -> m.containsElement(name))//
				.map(name -> m.getElement(name))//
				.collect(Collectors.toSet());
	}

	/**
	 * Get ids from a match. Note that edge and node ids are orthogonal. To avoid
	 * duplicate ids, edge ids are prepended with a - to retain uniqueness.
	 * 
	 * @param elements
	 * @param m
	 * @return
	 */
	protected Set<Long> extractRelIDs(Collection<String> rels, NeoMatch m) {
		return rels.stream()//
				.filter(name -> m.containsElement(name))//
				.map(name -> -1 * m.getElement(name))//
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

	public boolean isConsistent() throws Exception {
		determineInconsistentElements();
		return inconsistentElements.isEmpty();
	}
}
