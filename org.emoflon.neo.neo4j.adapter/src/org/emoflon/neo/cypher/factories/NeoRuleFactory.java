package org.emoflon.neo.cypher.factories;

import org.emoflon.neo.cypher.models.EmptyBuilder;
import org.emoflon.neo.cypher.models.IBuilder;
import org.emoflon.neo.cypher.rules.NeoRule;
import org.emoflon.neo.cypher.rules.RulePreProcessor;
import org.emoflon.neo.emsl.eMSL.Rule;
import org.emoflon.neo.emsl.refinement.EMSLFlattener;
import org.emoflon.neo.emsl.util.FlattenerException;

public class NeoRuleFactory {

	public static NeoRule createNeoRule(Rule r) {
		return createNeoRule(r, new EmptyBuilder());
	}

	public static NeoRule createNeoRule(Rule r, IBuilder builder) {
		try {
			var flatRule = (Rule) EMSLFlattener.flatten(r);
			var rulePreprocessor = new RulePreProcessor();
			rulePreprocessor.preprocess(flatRule);
			return new NeoRule(flatRule, builder);
		} catch (FlattenerException e) {
			e.printStackTrace();
			return null;
		}
	}

}
