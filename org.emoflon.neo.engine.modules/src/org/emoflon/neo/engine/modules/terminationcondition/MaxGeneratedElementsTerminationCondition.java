package org.emoflon.neo.engine.modules.terminationcondition;

import org.apache.log4j.Logger;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.ITerminationCondition;

public class MaxGeneratedElementsTerminationCondition implements ITerminationCondition<NeoMatch, NeoCoMatch> {
	private static final Logger logger = Logger.getLogger(MaxGeneratedElementsTerminationCondition.class);
	
	private long elementsAtStart;
	private long maxNoOfElements;
	private NeoCoreBuilder builder;

	public MaxGeneratedElementsTerminationCondition(long maxNoOfElements, NeoCoreBuilder builder) {
		this.maxNoOfElements= maxNoOfElements;
		this.builder = builder;
		start();
	}

	public void start() {
		elementsAtStart = builder.noOfElementsInDatabase();
	}

	@Override
	public boolean isReached(MatchContainer<NeoMatch, NeoCoMatch> matchContainer) {
		var currentNoOfElements = builder.noOfElementsInDatabase();
		if (currentNoOfElements - elementsAtStart >= maxNoOfElements)
			return true;
		else {
			logger.debug("Generated " + currentNoOfElements + " of " + maxNoOfElements);
			return false;
		}
	}
}
