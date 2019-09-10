package org.emoflon.neo.engine.generator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.emoflon.neo.engine.api.rules.ICoMatch;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.api.rules.IRule;

public class MatchContainer<M extends IMatch, C extends ICoMatch> {
	private Map<M, IRule<M, C>> matchesToRule;
	private Map<IRule<M, C>, Collection<M>> rulesToMatches;

	public MatchContainer(Collection<IRule<M, C>> pAllRules) {
		matchesToRule = new HashMap<>();
		rulesToMatches = new HashMap<>();
		pAllRules.forEach(rule -> rulesToMatches.put(rule, new HashSet<>()));
	}

	public void add(M pMatch, IRule<M, C> pRule) {
		if (!rulesToMatches.containsKey(pRule))
			throw new IllegalArgumentException("The specified rule does not exist in this MatchContainer");

		matchesToRule.put(pMatch, pRule);
		rulesToMatches.get(pRule).add(pMatch);
	}

	public IRule<M, C> remove(M pMatch) {
		if (!matchesToRule.containsKey(pMatch))
			return null;

		IRule<M, C> rule = matchesToRule.remove(pMatch);
		rulesToMatches.get(rule).remove(pMatch);
		return rule;
	}

	public Collection<IRule<M, C>> getRulesWithoutMatches() {
		return rulesToMatches.keySet().stream()//
				.filter(rule -> rulesToMatches.get(rule).isEmpty())//
				.collect(Collectors.toSet());
	}

	public Stream<M> stream() {
		return matchesToRule.keySet().stream();
	}

	public void clear() {
		matchesToRule.clear();
		rulesToMatches.clear();
	}
}
