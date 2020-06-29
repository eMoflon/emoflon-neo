package org.emoflon.neo.engine.modules.updatepolicies;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoRule;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.engine.generator.modules.ICleanupModule;
import org.emoflon.neo.engine.modules.ilp.ILPBasedOperationalStrategy;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;

public class ModelIntegrationOperationalStrategy extends ILPBasedOperationalStrategy implements ICleanupModule {
	private Collection<Long> createDeltaElements;
	private Collection<Long> deleteDeltaElements;
	private Collection<Long> existingElements;
	private Collection<Long> createdElements;

	private static final double alpha = -10; // delete-delta
	private static final double beta = 10; // create-delta
	private static final double gamma = -0.1; // added elements

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

	private String getGenRuleName(NeoMatch m) {
		return genRules.keySet().stream().filter(n -> m.getPattern().getName().startsWith(n)).findFirst().get();
	}

	@Override
	protected Set<Long> getContextElts(NeoMatch m) {
		var genRule = genRules.get(getGenRuleName(m));
		var ids = extractNodeIDs(genRule.getContextNodeLabels(), m);
		ids.addAll(extractRelIDs(genRule.getContextRelLabels(), m));
		return ids;
	}

	@Override
	protected Set<Long> getCreatedAndMarkedElts(NeoMatch m) {
		var genRule = genRules.get(getGenRuleName(m));
		var ids = extractNodeIDs(genRule.getCreatedNodeLabels(), m);
		ids.addAll(extractRelIDs(genRule.getCreatedRelLabels(), m));
		return ids;
	}

	private Collection<Long> determineCreateDeltaElements() {
		return builder.getCreateDelta(sourceModel, targetModel);
	}

	private Collection<Long> determineDeleteDeltaElements() {
		return builder.getDeleteDelta(sourceModel, targetModel);
	}

	private Collection<Long> determineExistingElements() {
		return builder.getExistingElements(sourceModel, targetModel);
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
					.filter(x -> relevantElements.contains(x) && (Long)x < 0)
					.collect(Collectors.toSet());
			
			var inconsistentNodes = inconsistentElts.stream()//
					.filter(x -> relevantElements.contains(x) && (Long)x > 0)
					.collect(Collectors.toSet());
			
			builder.deleteEdges(inconsistentEdges);
			inconsistentElts.removeAll(inconsistentEdges);
			builder.deleteNodes(inconsistentNodes);
			inconsistentElts.removeAll(inconsistentNodes);
		});
	}
}
