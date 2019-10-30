package org.emoflon.neo.engine.generator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.engine.api.rules.ICoMatch;
import org.emoflon.neo.engine.api.rules.IRule;

public abstract class MatchContainer<M extends IMatch, C extends ICoMatch> {
	private Map<IRule<M, C>, Collection<M>> rulesToMatches;
	protected Map<IRule<M, C>, Integer> ruleApplications;
	private NodeRange currentNodeRange;

	public MatchContainer(Collection<IRule<M, C>> allRules) {
		rulesToMatches = new HashMap<>();
		ruleApplications = new HashMap<>();
		allRules.forEach(rule -> {
			rulesToMatches.put(rule, new HashSet<>());
			ruleApplications.put(rule, 0);
		});
		
		currentNodeRange = new NodeRange();
	}

	public void removeRule(IRule<M, C> rule) {
		rulesToMatches.remove(rule);
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

	public Map<IRule<M, C>, Collection<M>> getAllRulesToMatches() {
		return Map.copyOf(rulesToMatches);
	}

	public Stream<M> matchesForRule(IRule<M, C> rule) {
		return rulesToMatches.get(rule).stream();
	}

	public abstract void appliedRule(IRule<M, C> rule, Collection<M> appliedMatches, Optional<Collection<C>> comatches);

	protected void addCreatedElementIDsToRange(C m, IRule<M, C> rule) {
		rule.getCreatedElts().forEach(elt -> {
			if (m.getNodeIDs().containsKey(elt))
				currentNodeRange.addIDs(getTypesFor(rule, elt), m.getNodeIDs().get(elt));
		});
	}

	protected abstract Stream<String> getTypesFor(IRule<M, C> rule, String elt);

	public int getNoOfRuleApplicationsFor(IRule<M, C> rule) {
		return ruleApplications.get(rule);
	}

	public Map<IRule<M, C>, Integer> getRuleApplications() {
		return Map.copyOf(ruleApplications);
	}

	public boolean hasNoMatches() {
		return streamAllMatches().count() == 0;
	}

	public NodeRange getNodeRange() {
		return currentNodeRange;
	}

	public Stream<IRule<M, C>> streamAllRules() {
		return rulesToMatches.keySet().stream();
	}

	public boolean hasNoRules() {
		return rulesToMatches.keySet().isEmpty();
	}

	public long getNumberOfGeneratedElements() {
		return currentNodeRange.getAllIDs().count();
	}

	public int getNumberOfRuleApplications() {
		return ruleApplications.entrySet().stream().map(entry -> entry.getValue()).reduce(0, Integer::sum);
	}
}
