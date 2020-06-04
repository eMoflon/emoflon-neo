package org.emoflon.neo.engine.modules.updatepolicies;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.cypher.rules.NeoRule;
import org.emoflon.neo.emsl.compiler.ops.BWD_OPT;
import org.emoflon.neo.emsl.compiler.ops.CC;
import org.emoflon.neo.emsl.compiler.ops.CO;
import org.emoflon.neo.emsl.compiler.ops.FWD_OPT;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.ICleanupModule;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.modules.ilp.ILPBasedOperationalStrategy;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;

public class ModelIntegrationOperationalStrategy extends ILPBasedOperationalStrategy implements ICleanupModule {
	private static final Logger logger = Logger.getLogger(CheckOnlyOperationalStrategy.class);
	private Optional<MatchContainer<NeoMatch, NeoCoMatch>> matchContainer = Optional.empty();
	private static final List<String> suffixes = List.of(new FWD_OPT().getNameExtension(), 
			new BWD_OPT().getNameExtension(), new CC().getNameExtension(), new CO().getNameExtension());
	private Collection<Long> createDeltaElements;
	private Collection<Long> deleteDeltaElements;
	private Collection<Long> existingElements;
	
	private static final double alpha = -3; // delete-delta
	private static final double beta = 3;  // create-delta
	private static final double gamma = -1; // added elements
	
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
	public Map<IRule<NeoMatch, NeoCoMatch>, Collection<NeoMatch>> selectMatches(//
			MatchContainer<NeoMatch, NeoCoMatch> matchContainer, //
			IMonitor<NeoMatch, NeoCoMatch> progressMonitor//
	) {
		if (this.matchContainer.isEmpty())
			this.matchContainer = Optional.of(matchContainer);

		return matchContainer.getAllRulesToMatches();
	}

	@Override
	public boolean isConsistent() throws Exception {
		if (inconsistentElements == null) {
			logger.debug("Registering all matches...");
			matchContainer.ifPresent(mc -> registerMatches(mc.streamAllCoMatches()));
			computeWeights();
			logger.debug("Registered all matches.");

			inconsistentElements = determineInconsistentElements();
		}

		return inconsistentElements.isEmpty();
	}

	@Override
	public void cleanup() {
		try {
			if (isConsistent()) {
				logger.info("Your triple is consistent!");
			} else {
				logger.info("Your triple is inconsistent!");
				var inconsistentElements = determineInconsistentElements();
				logger.info(inconsistentElements.size() + " elements of your triple are inconsistent!");
				logger.debug("Inconsistent element IDs: " + inconsistentElements);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		builder.removeContextDeltaAttributesFromModel(sourceModel, targetModel);
	}

	@Override
	public String description() {
		return "ILP solving, integrating source and target model";
	}
	
	@Override
	protected void computeWeights() {
		matchToWeight = new HashMap<>();
		createDeltaElements = determineCreateDeltaElements();
		deleteDeltaElements = determineDeleteDeltaElements();
		existingElements = determineExistingElements();
		
		for (var m : matchToId.keySet()) {		
			double weight = 0;
			
			for (Long e : getMarkedElts(m)) {
				if (createDeltaElements.contains(e))
					weight += beta;
				else if (deleteDeltaElements.contains(e))
					weight += alpha;
				else if (existingElements.contains(e))
					weight += 1;
				else
					weight += gamma;
			}
			matchToWeight.put(m, weight);
		}
	}
	
	private String getGenRuleName(NeoMatch m) {
		String genRuleName = m.getPattern().getName();
		for (String suffix : suffixes)
			genRuleName = genRuleName.replaceAll(suffix, "");
		return genRuleName;
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
	
	public Collection<Long> determineCreateDeltaElements() {
			return builder.getCreateDelta(sourceModel, targetModel);
	}
	
	public Collection<Long> determineDeleteDeltaElements() {
			return builder.getDeleteDelta(sourceModel, targetModel);
	}
	
	public Collection<Long> determineExistingElements() {
			return builder.getExistingElements(sourceModel, targetModel);
	}
}
