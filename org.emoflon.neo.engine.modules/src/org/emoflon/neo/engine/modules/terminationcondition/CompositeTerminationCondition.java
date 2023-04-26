package org.emoflon.neo.engine.modules.terminationcondition;

import java.util.ArrayList;
import java.util.List;

import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.ITerminationCondition;

public class CompositeTerminationCondition implements ITerminationCondition<NeoMatch, NeoCoMatch> {

	private List<ITerminationCondition<NeoMatch, NeoCoMatch>> terminationConditions;
	
	public CompositeTerminationCondition() {
		this.terminationConditions = new ArrayList<>();
	}
	
	public boolean add(ITerminationCondition<NeoMatch, NeoCoMatch> e) {
		return terminationConditions.add(e);
	}

	@Override
	public boolean isReached(MatchContainer<NeoMatch, NeoCoMatch> matchContainer) {
		if(terminationConditions.isEmpty())
			return true;
		for(ITerminationCondition<NeoMatch, NeoCoMatch> terminationCondition : terminationConditions) {
			if(terminationCondition.isReached(matchContainer))
				return true;
		}
		return false;
	}
}
