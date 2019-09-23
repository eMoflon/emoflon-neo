package org.emoflon.neo.engine.modules.updatepolicies;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.modules.ilp.ILPBasedOperationalStrategy;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

public class CheckOnlyOperationalStrategy extends ILPBasedOperationalStrategy {

	public CheckOnlyOperationalStrategy(Collection<IRule<NeoMatch, NeoCoMatch>> genRules, Collection<IConstraint> negativeConstraints, boolean optimise) {
		super(genRules, negativeConstraints, optimise);
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

	/**
	 * Get ids from a match. Note that edge and node ids are orthogonal. To avoid
	 * duplicate ids, edge ids are prepended with a - to retain uniqueness.
	 * 
	 * @param elements
	 * @param m
	 * @return
	 */
	private Set<Long> extractIDs(Stream<String> elements, IMatch m) {
		return elements//
				.filter(name -> m.getNodeIDs().containsKey(name) || m.getEdgeIDs().containsKey(name))//
				.map(name -> m.getNodeIDs().containsKey(name) ? //
						m.getNodeIDs().get(name) : -1 * m.getEdgeIDs().get(name))//
				.collect(Collectors.toSet());
	}
}
