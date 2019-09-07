package org.emoflon.neo.engine.modules;

import java.util.HashMap;
import java.util.Map;

import org.emoflon.neo.emsl.eMSL.GraphGrammar;
import org.emoflon.neo.emsl.eMSL.Rule;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.IRuleScheduler;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoRuleFactory;

public abstract class AbstractNeoRuleScheduler implements IRuleScheduler<NeoMatch, NeoCoMatch> {

	protected GraphGrammar grammar;
	protected Map<Rule, IRule<NeoMatch, NeoCoMatch>> rules;

	public AbstractNeoRuleScheduler(GraphGrammar pGrammar) {
		grammar = pGrammar;
		rules = new HashMap<>();

		// TODO is this right? -> no it's not, use access instead
		grammar.getRules().forEach(rule -> rules.put(rule, NeoRuleFactory.createNeoRule(rule, null, null)));
	}
}
