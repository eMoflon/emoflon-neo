package org.emoflon.neo.engine.generator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Stream;

import org.emoflon.neo.engine.api.rules.ICoMatch;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.api.rules.IRule;

public class MatchContainer<M extends IMatch, C extends ICoMatch> {
	private Map<M, IRule<M, C>> matchesToRule;
	private Map<IRule<M, C>, Collection<M>> rulesToMatches;

	public MatchContainer() {
		matchesToRule = new HashMap<>();
		rulesToMatches = new HashMap<>();
	}

	public void add(M pMatch, IRule<M, C> pRule) {
		matchesToRule.put(pMatch, pRule);
		if (!rulesToMatches.containsKey(pRule))
			rulesToMatches.put(pRule, new HashSet<M>());
		rulesToMatches.get(pRule).add(pMatch);
	}

	public IRule<M, C> remove(M pMatch) {
		IRule<M, C> rule = matchesToRule.remove(pMatch);
		rulesToMatches.get(rule).remove(pMatch);
		return rule;
	}

	public Stream<M> stream() {
		return matchesToRule.keySet().stream();
	}

	public void clear() {
		matchesToRule.clear();
		rulesToMatches.clear();
	}
}
