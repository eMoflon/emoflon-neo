package org.emoflon.neo.engine.modules.updatepolicies;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.cypher.rules.NeoRule;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.ICleanupModule;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.modules.ilp.ILPBasedOperationalStrategy;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;

public class ModelIntegrationOperationalStrategy extends ILPBasedOperationalStrategy implements ICleanupModule {
	private Collection<Long> createDeltaElements;
	private Collection<Long> deleteDeltaElements;
	private Collection<Long> existingElements;
	private Collection<Long> createdElements;
	private HashMap<String, NeoRule> opRuleNameToGenRule;

	private static double alpha; // delete-delta
	private static double beta; // create-delta
	private static double gamma; // added elements

	public ModelIntegrationOperationalStrategy(//
			SupportedILPSolver solver, //
			NeoCoreBuilder builder, //
			Collection<NeoRule> genRules, //
			Collection<NeoRule> opRules, //
			Collection<IConstraint> negativeConstraints, //
			String sourceModel, //
			String targetModel//
	) {
		super(solver, genRules, opRules, negativeConstraints, builder, sourceModel, targetModel);
		setWeightings(builder.getAlpha(), builder.getBeta(), builder.getGamma());
		opRuleNameToGenRule = new HashMap<>();
		
		for (NeoRule opRule : opRules) {
			// Check whether it is a CO rule
			NeoRule genRule = (NeoRule)(super.genRules.get(opRule.getName()));
			
			if (genRule == null) {
				// Other operational rule
				int splitIndex  = opRule.getName().lastIndexOf("_");
				String genRuleName = opRule.getName().substring(0, splitIndex);
				genRule = (NeoRule)(super.genRules.get(genRuleName));
			}
			
			opRuleNameToGenRule.put(opRule.getName(), genRule);
		}
	}

	@Override
	public boolean isConsistent() throws Exception {
		if (inconsistentElements == null) {

			logger.debug("Registering all matches...");
			matchContainer.ifPresent(mc -> registerMatches(mc.streamAllCoMatches()));
			logger.debug("Registered all matches.");
			
			logger.debug("Determine element sets...");
			createDeltaElements = determineCreateDeltaElements();
			deleteDeltaElements = determineDeleteDeltaElements();
			existingElements = determineExistingElements();
			createdElements = determineCreatedElements();
			logger.debug("Element sets determined.");
			computeWeights();

			inconsistentElements = determineInconsistentElements();
			removeInconsistentElements(inconsistentElements);
		}

		return inconsistentElements.isEmpty();
	}

	@Override
	public void cleanup() {
		super.cleanup();
		builder.removeContextDeltaAttributesFromModel(sourceModel, targetModel);
	}

	@Override
	public String description() {
		return "ILP solving, integrating source and target model";
	}

	@Override
	protected void computeWeights() {
		matchToWeight = new HashMap<>();

		for (var m : matchToId.keySet()) {
			double weight = 0;

			logger.debug("ID: " + matchToId.get(m) + ", Match:" + m.getPattern().getName());
			for (Long e : getCreatedAndMarkedElts(m)) {
				if (createDeltaElements.contains(e))
					weight += beta;
				else if (deleteDeltaElements.contains(e))
					weight += alpha;
				else if (existingElements.contains(e))
					weight += 1;
				else if (createdElements.contains(e))
					weight += gamma;
			}
			matchToWeight.put(m, weight);
			logger.debug("Match:" + m.getPattern().getName() + ", Gewicht: " + weight);
		}
	}

	private NeoRule getGenRule(NeoMatch m) {
		return opRuleNameToGenRule.get(m.getPattern().getName());	
	}
	
	@Override
	protected Set<Long> getContextElts(NeoMatch m) {
		var genRule = getGenRule(m);
		var ids = extractNodeIDs(genRule.getContextNodeLabels(), m);
		ids.addAll(extractRelIDs(genRule.getContextRelLabels(), m));
		return ids;
	}

	@Override
	protected Set<Long> getCreatedAndMarkedElts(NeoMatch m) {
		var genRule = getGenRule(m);
		var ids = extractNodeIDs(genRule.getCreatedNodeLabels(), m);
		ids.addAll(extractRelIDs(genRule.getCreatedRelLabels(), m));
		return ids;
	}

	private Collection<Long> determineCreateDeltaElements() {
		if (createDeltaElements == null)
			createDeltaElements = builder.getCreateDelta(sourceModel, targetModel);
		return createDeltaElements;
	}

	private Collection<Long> determineDeleteDeltaElements() {
		if (deleteDeltaElements == null)
			deleteDeltaElements = builder.getDeleteDelta(sourceModel, targetModel);
		return deleteDeltaElements;
	}

	private Collection<Long> determineExistingElements() {
		if (existingElements == null)
			existingElements = builder.getExistingElements(sourceModel, targetModel);
		return existingElements;
	}

	private Collection<Long> determineCreatedElements() {
		Collection<Long> createdElements = builder.getAllCorrs(sourceModel, targetModel);
		createdElements.addAll(builder.getAllElementsOfModel(sourceModel));
		createdElements.addAll(builder.getAllElementsOfModel(targetModel));
		createdElements.removeAll(determineExistingElements());
		return createdElements;
	}

	@Override
	protected void removeInconsistentElements(Collection<Long> inconsistentElts) {
		Set<Long> relevantElements = new HashSet<Long>();
		relevantElements.addAll(builder.getAllElementsOfModel(sourceModel));
		relevantElements.addAll(builder.getAllCorrs(sourceModel, targetModel));
		relevantElements.addAll(builder.getAllElementsOfModel(targetModel));
				
		matchContainer.ifPresent(mc -> {
			var inconsistentEdges = inconsistentElts.stream()//
					.filter(x -> relevantElements.contains(x) && x < 0)
					.collect(Collectors.toSet());
			
			var inconsistentNodes = inconsistentElts.stream()//
					.filter(x -> relevantElements.contains(x) && x > 0)
					.collect(Collectors.toSet());
			
			builder.deleteEdges(inconsistentEdges);
			inconsistentElts.removeAll(inconsistentEdges);
			builder.deleteNodes(inconsistentNodes);
			inconsistentElts.removeAll(inconsistentNodes);
		});
	}
	
	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Collection<NeoMatch>> selectMatches(//
			MatchContainer<NeoMatch, NeoCoMatch> matchContainer, //
			IMonitor<NeoMatch, NeoCoMatch> progressMonitor//
	) {
		var filteredRulesToMatches = new HashMap<IRule<NeoMatch, NeoCoMatch>, Collection<NeoMatch>>();
		var allRulesToMatches =  super.selectMatches(matchContainer, progressMonitor);
		determineCreateDeltaElements();
		Collection<Long> createDeltaElementsWithPositiveIds = createDeltaElements.stream().map(e -> Math.abs(e)).collect(Collectors.toList());
		
		for (IRule<NeoMatch,NeoCoMatch> r : allRulesToMatches.keySet()) {
			// No CO rule
			if (r.getCreatedNodeLabels().isEmpty() && r.getCreatedRelLabels().isEmpty()) 
				filteredRulesToMatches.put(r, allRulesToMatches.get(r));
			else {
				filteredRulesToMatches.put(r, new HashSet<NeoMatch>());
				for (NeoMatch m : allRulesToMatches.get(r)) 
					if (m.getElements().stream().filter(e -> createDeltaElementsWithPositiveIds.contains(e)).findAny().isPresent())
						filteredRulesToMatches.get(r).add(m);
			}
		}
		return filteredRulesToMatches;
	}
	
	public static void setWeightings(double alpha, double beta, double gamma) {
		ModelIntegrationOperationalStrategy.alpha = alpha;
		ModelIntegrationOperationalStrategy.beta = beta;
		ModelIntegrationOperationalStrategy.gamma = gamma;
	}
}
