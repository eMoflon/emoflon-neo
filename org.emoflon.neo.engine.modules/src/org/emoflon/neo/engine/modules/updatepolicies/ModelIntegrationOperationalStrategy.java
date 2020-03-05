package org.emoflon.neo.engine.modules.updatepolicies;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
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
	private static final Logger logger = Logger.getLogger(CheckOnlyOperationalStrategy.class);
	private Optional<MatchContainer<NeoMatch, NeoCoMatch>> matchContainer = Optional.empty();
	
	private Collection<Long> createDeltaElements;
	private Collection<Long> deleteDeltaElements;
	private Collection<Long> addedElements;
	
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
		//TODO Implement suitable cleanup module
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
		addedElements = determineAddedElements();
		
		for (var m : matchToId.keySet()) {		
			double weight = 0;
			
			for (Long e : getMarkedElts(m)) {
				if (createDeltaElements.contains(e))
					weight += beta;
				else if (deleteDeltaElements.contains(e))
					weight += alpha;
				else if (addedElements.contains(e))
					weight += gamma;
				else
					weight += 1;
			}
			matchToWeight.put(m, weight);
		}
	}
	
	public Collection<Long> determineCreateDeltaElements() {
			return builder.getCreateDelta(sourceModel, targetModel);
	}
	
	public Collection<Long> determineDeleteDeltaElements() {
			return builder.getDeleteDelta(sourceModel, targetModel);
	}
	
	public Collection<Long> determineAddedElements() {
			return builder.getAddedDelta(sourceModel, targetModel);
	}
}
