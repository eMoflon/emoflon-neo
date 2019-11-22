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

public abstract class MatchContainer<M extends IMatch, C extends ICoMatch> {
	private Map<IRule<M, C>, Collection<M>> rulesToMatches;
	protected Map<IRule<M, C>, Integer> ruleApplications;
	protected Collection<C> coMatches;
	private ElementRange currentNodeRange;
	private ElementRange currentRelRange;
	private long noOfGeneratedElements = 0;

	public MatchContainer(Collection<? extends IRule<M, C>> allRules) {
		rulesToMatches = new HashMap<>();
		ruleApplications = new HashMap<>();
		coMatches = new ArrayList<>();
		allRules.forEach(rule -> {
			rulesToMatches.put(rule, new HashSet<>());
			ruleApplications.put(rule, 0);
		});

		currentNodeRange = new ElementRange();
		currentRelRange = new ElementRange();
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

	public abstract void appliedRule(IRule<M, C> rule, Collection<M> appliedMatches, Collection<C> comatches);

	protected void addCreatedElementIDsToRange(C m, IRule<M, C> rule) {
		rule.getCreatedNodeLabels().forEach(elt -> {
			noOfGeneratedElements++;
			if (m.containsElement(elt))
				currentNodeRange.addIDs(getTypesForNode(rule, elt), m.getElement(elt));
		});
		
		rule.getCreatedRelLabels().forEach(elt -> {
			noOfGeneratedElements++;
			if(m.containsElement(elt))
				currentRelRange.addID(getTypeForRel(rule, elt), m.getElement(elt));
		});
		
	}

	protected abstract String getTypeForRel(IRule<M, C> rule, String relNameAccordingToConvention);

	protected abstract Stream<String> getTypesForNode(IRule<M, C> rule, String nodeName);

	public int getNoOfRuleApplicationsFor(IRule<M, C> rule) {
		return ruleApplications.get(rule);
	}

	public Map<IRule<M, C>, Integer> getRuleApplications() {
		return Map.copyOf(ruleApplications);
	}

	public boolean hasNoMatches() {
		return streamAllMatches().count() == 0;
	}

	public ElementRange getNodeRange() {
		return currentNodeRange;
	}
	
	public ElementRange getRelRange() {
		return currentRelRange;
	}

	public Stream<IRule<M, C>> streamAllRules() {
		return rulesToMatches.keySet().stream();
	}

	public boolean hasNoRules() {
		return rulesToMatches.keySet().isEmpty();
	}

	public long getNumberOfGeneratedElements() {
		return noOfGeneratedElements;
	}

	public int getNumberOfRuleApplications() {
		return ruleApplications.entrySet().stream().map(entry -> entry.getValue()).reduce(0, Integer::sum);
	}

	public Stream<C> streamAllCoMatches() {
		return coMatches.stream();
	}
}
