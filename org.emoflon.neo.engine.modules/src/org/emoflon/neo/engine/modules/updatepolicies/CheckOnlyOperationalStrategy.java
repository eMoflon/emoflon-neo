package org.emoflon.neo.engine.modules.updatepolicies;

import java.util.Collection;
import java.util.Set;

import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.modules.ilp.ILPBasedOperationalStrategy;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

public class CheckOnlyOperationalStrategy extends ILPBasedOperationalStrategy {

	public CheckOnlyOperationalStrategy(Collection<IRule<NeoMatch, NeoCoMatch>> genRules, Collection<IConstraint> negativeConstraints) {
		super(genRules, negativeConstraints);
	}

	@Override
	protected Set<Long> getContextElts(IMatch m) {
		var genRule = genRules.get(m.getPattern().getName());
		return extractIDs(genRule.getContextElts(), m);
	}

	@Override
	protected Set<Long> getCreatedElts(IMatch m) {
		var genRule = genRules.get(m.getPattern().getName());
		return extractIDs(genRule.getCreatedElts(), m);
	}
	
	public boolean isConsistent(SupportedILPSolver suppSolver) throws Exception {
		var result = determineInconsistentElements(suppSolver, false);
		return result.filter(elts -> elts.isEmpty()).isPresent();
	}
}
