package org.emoflon.neo.emsl.compiler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.emoflon.neo.emsl.EMSLFlattener;
import org.emoflon.neo.emsl.eMSL.EMSLFactory;
import org.emoflon.neo.emsl.eMSL.GraphGrammar;
import org.emoflon.neo.emsl.eMSL.Rule;
import org.emoflon.neo.emsl.eMSL.TripleGrammar;
import org.emoflon.neo.emsl.eMSL.TripleRule;
import org.emoflon.neo.emsl.util.FlattenerException;

import com.google.common.collect.Lists;

public class TGGCompiler {

	private EMSLFlattener flattener = new EMSLFlattener();
	private Map<TripleGrammar, Set<TripleRule>> tggToRulesMap;
	private Map<TripleGrammar, GraphGrammar> tggs;

	public TGGCompiler(Iterable<TripleGrammar> pTGGs, Iterable<TripleRule> pTGGRules) {
		if (pTGGs == null)
			throw new IllegalArgumentException("");

		tggToRulesMap = new HashMap<>();
		pTGGs.forEach(tgg -> tggToRulesMap.put(tgg, new HashSet<>()));
		pTGGRules.forEach(rule -> tggToRulesMap.get(rule.getType()).add(rule));

		tggs = new HashMap<>();
		pTGGs.forEach(tgg -> tggs.put(tgg, null));
	}

	public void compile() {
		tggs.keySet().forEach(tgg -> tggs.put(tgg, compileTGG(tgg)));
	}

	private GraphGrammar compileTGG(TripleGrammar pTGG) {
		GraphGrammar graphGrammar = EMSLFactory.eINSTANCE.createGraphGrammar();
		graphGrammar.setName(pTGG.getName()); // TODO naming convention?
		tggToRulesMap.get(pTGG).forEach(rule -> graphGrammar.getRules().add(compileTGGRule(rule)));
		return graphGrammar;
	}

	private Rule compileTGGRule(TripleRule pRule) {

		try {
			flattener.flattenEntity(pRule, Lists.newArrayList());
		} catch (FlattenerException pFE) {
			// TODO what to do here? when can this even happen?
		}

		Rule rule = EMSLFactory.eINSTANCE.createRule();
		rule.setName(pRule.getName()); // TODO naming convention?
		rule.setAbstract(pRule.isAbstract());
		rule.getNodeBlocks().addAll(pRule.getSrcNodeBlocks());
		rule.getNodeBlocks().addAll(pRule.getTrgNodeBlocks());

		// TODO discuss with Tony
		// attribute constraints to conditions
		// correspondences to relations

		return rule;
	}
}
