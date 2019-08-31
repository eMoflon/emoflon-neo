package org.emoflon.neo.generator.modules;

import java.util.Map;

import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.generator.IMatchReprocessor;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

/**
 * Reprocessor that simply discards all remaining matches.
 */
public class ParanoidNeoReprocessor implements IMatchReprocessor<NeoMatch, NeoCoMatch> {
	@Override
	public void reprocess(Map<NeoMatch, IRule<NeoMatch, NeoCoMatch>> pRemainingMatches) {
		pRemainingMatches.clear();
	}
}
