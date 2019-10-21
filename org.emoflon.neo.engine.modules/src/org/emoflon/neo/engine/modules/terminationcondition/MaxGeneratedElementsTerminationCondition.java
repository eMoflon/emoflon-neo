package org.emoflon.neo.engine.modules.terminationcondition;

import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.ITerminationCondition;
import org.emoflon.neo.neo4j.adapter.models.NeoCoreBuilder;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

public class MaxGeneratedElementsTerminationCondition implements ITerminationCondition<NeoMatch, NeoCoMatch> {
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
		if (builder.noOfElementsInDatabase() - elementsAtStart >= maxNoOfElements)
			return true;
		else
			return false;
	}
}
