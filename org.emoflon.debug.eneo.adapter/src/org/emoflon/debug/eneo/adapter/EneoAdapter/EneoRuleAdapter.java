package org.emoflon.debug.eneo.adapter.EneoAdapter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.emoflon.ibex.tgg.ui.debug.api.Graph;
import org.emoflon.ibex.tgg.ui.debug.api.Rule;
import org.emoflon.ibex.tgg.ui.debug.api.impl.GraphBuilder;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

public class EneoRuleAdapter implements Rule {
    private static Map<String, EneoRuleAdapter> rulesByName = new HashMap<>();

    public static EneoRuleAdapter getRuleByName(String pRuleName) {
	return rulesByName.get(pRuleName);
    }

    public static Collection<Rule> getAllRules() {
	return Collections.unmodifiableCollection(rulesByName.values());
    }

    public static EneoRuleAdapter adapt(IRule<NeoMatch, NeoCoMatch> pRule) {
	if (!rulesByName.containsKey(pRule.getName())) {
	    EneoRuleAdapter rule = new EneoRuleAdapter(pRule);
	    rulesByName.put(rule.getName(), rule);
	}
	return rulesByName.get(pRule.getName());
    }

    private IRule<NeoMatch, NeoCoMatch> rule;
    private Graph graph;

    private EneoRuleAdapter(IRule<NeoMatch, NeoCoMatch> pRule) {
	rule = pRule;
	GraphBuilder graphBuilder = new GraphBuilder();

	// TODO: Build the graph
	// TODO: Add nodes to the graph

	// TODO: Add edges (corr and regular) to the graph

	graph = graphBuilder.build();

    }

    @Override
    public String getName() {
	
	return rule.getName();
    }

    @Override
    public Graph getGraph() {
	// TODO Auto-generated method stub
	return null;
    }

}
