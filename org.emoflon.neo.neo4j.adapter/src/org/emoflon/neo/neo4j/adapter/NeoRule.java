package org.emoflon.neo.neo4j.adapter;

import java.util.Collection;
import java.util.Optional;

import org.emoflon.neo.emsl.eMSL.Rule;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.api.rules.RuleApplicationSemantics;

// TODO [Jannik]
public class NeoRule implements IRule<NeoMatch, NeoCoMatch> {

	public NeoRule(Rule r, NeoCoreBuilder builder, NeoMask mask) {
		// TODO[Jannik] Use mask to fix parameters for the query
		this(r, builder);
	}

	public NeoRule(Rule r, NeoCoreBuilder builder) {

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMatchInjectively(Boolean injective) {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<NeoMatch> determineMatches() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<NeoMatch> determineMatches(int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<NeoCoMatch> apply(NeoMatch match, RuleApplicationSemantics ras) {
		// TODO Auto-generated method stub
		return null;
	}

}
