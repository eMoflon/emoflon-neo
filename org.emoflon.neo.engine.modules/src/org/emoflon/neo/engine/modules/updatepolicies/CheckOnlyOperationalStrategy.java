package org.emoflon.neo.engine.modules.updatepolicies;

import java.util.Collection;
import java.util.Collections;

import org.emoflon.neo.engine.api.rules.ITripleRule;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IUpdatePolicy;
import org.emoflon.neo.engine.modules.ilp.OPT;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

public class CheckOnlyOperationalStrategy extends OPT implements IUpdatePolicy<NeoMatch, NeoCoMatch> {

	public CheckOnlyOperationalStrategy(Collection<ITripleRule> tripleRules) {
		// TODO[Anjorin] Keep for dependency analysis
	}
	
	@Override
	protected void computeWeights() {
		// TODO[Anjorin] Compute weights for matches (matchToWeight)
	}

	@Override
	public Collection<NeoMatch> selectMatches(MatchContainer<NeoMatch, NeoCoMatch> pMatches,
			IMonitor pProgressMonitor) {
		// TODO[Anjorin] Perform CheckOnly operation here
		return Collections.emptySet();
	}
}
