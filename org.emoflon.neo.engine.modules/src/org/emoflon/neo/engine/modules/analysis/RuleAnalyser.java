package org.emoflon.neo.engine.modules.analysis;

import org.emoflon.neo.emsl.eMSL.Rule;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.neo4j.adapter.rules.NeoRule;

/**
 * Some Basic Terminology:
 * 
 * <li>An <b>axiom</b> is a rule with no context elements.</li>
 * 
 * <li>A <b>free</b> rule is a rule without any NACs.</li>
 * 
 * <li>The <b>element creation pool</b> is a collection of elements that have
 * been created in a given time span.</li>
 * 
 * <li>The <b>type creation pool</b> is the set of the types of all elements in
 * the element creation pool.</li>
 * 
 * <li>A rule is <b>activated</b> if all types of all its context elements are
 * in the type creation pool.</li>
 * 
 * <li>A rule has <b>maxed out</b> if it has been applied more times than a
 * provided maximum.</li>
 * 
 * <li>A match is <b>volatile</b> if its rule is not free</li>
 * 
 * <li>A match is <b>valid</b> it cannot be extended to violate a NAC of its
 * rule</li>
 * 
 * @author aanjorin
 */
public class RuleAnalyser {
	
	public static Rule toRule(IRule<?,?> rule) {
		return ((NeoRule)rule).getEMSLRule();
	}
	
	public static boolean isFreeAxiom(Rule rule) {
		return isAxiom(rule) && isFree(rule);
	}

	public static boolean isFree(Rule rule) {
		return rule.getCondition() == null;
	}

	private static boolean isAxiom(Rule rule) {
		return rule.getNodeBlocks().stream().noneMatch(nb -> nb.getAction() == null);
	}
}
