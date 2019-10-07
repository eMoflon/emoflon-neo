package org.emoflon.neo.engine.generator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.engine.api.rules.ICoMatch;
import org.emoflon.neo.engine.api.rules.IRule;

public class MatchContainer<M extends IMatch, C extends ICoMatch> {
	private Map<IRule<M, C>, Collection<M>> rulesToMatches;

	public MatchContainer(Collection<IRule<M, C>> allRules) {
		rulesToMatches = new HashMap<>();
		allRules.forEach(rule -> rulesToMatches.put(rule, new HashSet<>()));
	}

	public void addAll(Collection<M> matches, IRule<M, C> rule) {
		if (!rulesToMatches.containsKey(rule))
			throw new IllegalArgumentException("The specified rule does not exist in this MatchContainer");

		rulesToMatches.get(rule).addAll(matches);
	}

	public List<IRule<M, C>> getRulesWithoutMatches() {
		return rulesToMatches.keySet().stream()//
				.filter(rule -> rulesToMatches.get(rule).isEmpty())//
				.collect(Collectors.toList());
	}

	public List<IRule<M, C>> getRulesWithMatches() {
		return rulesToMatches.keySet().stream()//
				.filter(rule -> !rulesToMatches.get(rule).isEmpty())//
				.collect(Collectors.toList());
	}
	
	public void clear() {
		rulesToMatches.forEach((r, matches) -> matches.clear());
	}
	
	public Stream<M> streamAllMatches() {
		return rulesToMatches.values().stream().flatMap(matches -> matches.stream());
	}
	
	public Stream<Entry<IRule<M, C>, Collection<M>>> stream() {
		return rulesToMatches.entrySet().stream();
	}
	
	public Map<IRule<M, C>, Collection<M>> getAllRulesToMatches(){
		return Map.copyOf(rulesToMatches);
	}
	
	public Stream<M> matchesForRule(IRule<M, C> rule){
		return rulesToMatches.get(rule).stream();
	}
}
