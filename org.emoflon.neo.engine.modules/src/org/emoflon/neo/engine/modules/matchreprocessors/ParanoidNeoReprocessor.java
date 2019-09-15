package org.emoflon.neo.engine.modules.matchreprocessors;

import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.IMatchReprocessor;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

/**
 * Reprocessor that simply discards all remaining matches.
 */
public class ParanoidNeoReprocessor implements IMatchReprocessor<NeoMatch, NeoCoMatch> {
	@Override
	public void reprocess(MatchContainer<NeoMatch, NeoCoMatch> pRemainingMatches, IMonitor pProgressMonitor) {
		pRemainingMatches.clear();
	}
}
