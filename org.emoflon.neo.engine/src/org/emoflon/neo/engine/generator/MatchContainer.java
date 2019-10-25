package org.emoflon.neo.engine.generator;

import java.util.ArrayList;
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
	private Collection<M> pristineMatches;
	private Map<IRule<M, C>, Collection<M>> rulesToMatches;
	private Map<IRule<M, C>, Integer> ruleApplications;

	public MatchContainer(Collection<IRule<M, C>> allRules) {
		rulesToMatches = new HashMap<>();
		ruleApplications = new HashMap<>();
		pristineMatches = new ArrayList<>();
		allRules.forEach(rule -> {
			rulesToMatches.put(rule, new HashSet<>());
			ruleApplications.put(rule, 0);
		});
	}

	public void removeRule(IRule<M, C> rule) {
		var matches = rulesToMatches.remove(rule);
		if(matches != null)
			pristineMatches.removeAll(matches);
	}
	
	public void addAll(Collection<M> matches, IRule<M, C> rule) {
		if (!rulesToMatches.containsKey(rule))
			throw new IllegalArgumentException("The specified rule does not exist in this MatchContainer");

		rulesToMatches.get(rule).addAll(matches);
		pristineMatches.addAll(matches);
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

	public Map<IRule<M, C>, Collection<M>> getAllRulesToMatches() {
		return Map.copyOf(rulesToMatches);
	}

	public Stream<M> matchesForRule(IRule<M, C> rule) {
		return rulesToMatches.get(rule).stream();
	}

	public void appliedRule(IRule<M, C> rule, Collection<M> appliedMatches) {
		int noOfApplications = appliedMatches.size();
		var before = ruleApplications.get(rule);
		var after = before + noOfApplications;
		ruleApplications.put(rule, after);
		pristineMatches.removeAll(appliedMatches);
	}

	public int getNoOfRuleApplicationsFor(IRule<M, C> rule) {
		return ruleApplications.get(rule);
	}

	public Map<IRule<M, C>, Integer> getRuleApplications() {
		return Map.copyOf(ruleApplications);
	}

	public void removeMatch(IRule<M, C> chosenRule, M chosenMatch) {
		rulesToMatches.get(chosenRule).remove(chosenMatch);
		pristineMatches.remove(chosenMatch);
	}

	public boolean hasNoMatches() {
		return streamAllMatches().findAny().isEmpty();
	}

	public boolean isPristine(M chosenMatch) {
		return pristineMatches.contains(chosenMatch);
	}
}
