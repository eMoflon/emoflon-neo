package org.emoflon.neo.engine.modules.updatepolicies;

import java.util.Collection;
import java.util.Map;

import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IUpdatePolicy;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

public class AllMatchesUpdatePolicy implements IUpdatePolicy<NeoMatch, NeoCoMatch> {
	@Override
	public Map<IRule<NeoMatch, NeoCoMatch>, Collection<NeoMatch>> selectMatches(MatchContainer<NeoMatch, NeoCoMatch> matches,
			IMonitor<NeoMatch, NeoCoMatch> progressMonitor) {
		return matches.getAllRulesToMatches();
	}
}
