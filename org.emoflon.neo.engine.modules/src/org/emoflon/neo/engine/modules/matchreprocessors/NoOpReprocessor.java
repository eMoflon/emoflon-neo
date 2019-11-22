package org.emoflon.neo.engine.modules.matchreprocessors;

import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.IMatchReprocessor;
import org.emoflon.neo.engine.generator.modules.IMonitor;

public class NoOpReprocessor implements IMatchReprocessor<NeoMatch, NeoCoMatch> {

	@Override
	public void reprocess(MatchContainer<NeoMatch, NeoCoMatch> remainingMatches, IMonitor<NeoMatch, NeoCoMatch> progressMonitor) {
		// No op
	}

}
