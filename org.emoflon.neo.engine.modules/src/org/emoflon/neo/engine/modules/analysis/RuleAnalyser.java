package org.emoflon.neo.engine.modules.analysis;

import java.util.Collection;
import java.util.Optional;

import org.emoflon.neo.cypher.rules.NeoRule;
import org.emoflon.neo.emsl.eMSL.Rule;
import org.emoflon.neo.emsl.eMSL.TripleRule;
import org.emoflon.neo.engine.api.rules.IRule;

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

	public static Rule toRule(IRule<?, ?> rule) {
		return ((NeoRule) rule).getEMSLRule();
	}

	public static boolean isFreeAxiom(Rule rule) {
		return isAxiom(rule) && isFree(rule);
	}

	public static boolean isFree(Rule rule) {
		return rule.getCondition() == null;
	}

	public static boolean isAxiom(Rule rule) {
		return rule.getNodeBlocks().stream().noneMatch(nb -> nb.getAction() == null);
	}

	/**
	 * Checks if the provided rule has (i) no corr context elements and, (ii) no
	 * context element of a type in one of the provided relevant metamodels.
	 * 
	 * @param rule
	 * @param relevantMetamodels
	 * @return
	 */
	public static boolean noRelevantContext(TripleRule rule, boolean SRC, boolean CORR, boolean TRG) {
		return noRelevantNodeContext(rule, SRC, TRG)//
				&& noRelevantRelContext(rule, SRC, TRG)//
				&& noCorrContext(rule, CORR);
	}

	public static boolean noRelevantContext(Optional<TripleRule> tripleRule, boolean SRC, boolean CORR, boolean TRG) {
		return tripleRule.map(tr -> noRelevantContext(tr, SRC, CORR, TRG)).orElse(false);
	}

	private static boolean noRelevantRelContext(TripleRule rule, boolean SRC, boolean TRG) {
		var noRelevantRelContext = true;
		if (SRC)
			noRelevantRelContext = noRelevantRelContext && rule.getSrcNodeBlocks().stream()//
					.flatMap(n -> n.getRelations().stream())//
					.noneMatch(r -> r.getAction() == null);
		if (TRG)
			noRelevantRelContext = noRelevantRelContext && rule.getTrgNodeBlocks().stream()//
					.flatMap(n -> n.getRelations().stream())//
					.noneMatch(r -> r.getAction() == null);

		return noRelevantRelContext;
	}

	private static boolean noRelevantNodeContext(TripleRule rule, boolean SRC, boolean TRG) {
		var noRelevantNodeContext = true;
		if (SRC)
			noRelevantNodeContext = noRelevantNodeContext
					&& rule.getSrcNodeBlocks().stream().noneMatch(n -> n.getAction() == null);
		if (TRG)
			noRelevantNodeContext = noRelevantNodeContext
					&& rule.getTrgNodeBlocks().stream().noneMatch(n -> n.getAction() == null);

		return noRelevantNodeContext;
	}

	private static boolean noCorrContext(TripleRule rule, boolean CORR) {
		return CORR && rule.getCorrespondences().stream()//
				.noneMatch(r -> r.getAction() == null);
	}

	public static Optional<TripleRule> toTripleRule(String ruleName, Collection<TripleRule> tripleRules) {
		return tripleRules.stream()//
				.filter(tr -> tr.getName().equals(ruleName))//
				.findAny();
	}
}
