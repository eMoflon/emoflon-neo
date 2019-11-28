package org.emoflon.neo.engine.modules.analysis;

import java.util.Collection;
import java.util.Optional;

import org.emoflon.neo.cypher.rules.NeoRule;
import org.emoflon.neo.emsl.compiler.TGGCompiler;
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
	public static boolean hasRelevantContext(String ruleName, Collection<TripleRule> tripleRules, boolean SRC,
			boolean CORR, boolean TRG) {
		var tripleRule = toTripleRule(ruleName, tripleRules);
		if (tripleRule.isPresent()) {
			return hasRelevantContext(tripleRule.get(), SRC, CORR, TRG);
		} else {
			// Triple rule does not exist, so it must have been added by the compiler
			switch (ruleName) {
			case TGGCompiler.CREATE_SRC_MODEL_RULE:
				return SRC;
			case TGGCompiler.CREATE_TRG_MODEL_RULE:
				return TRG;
			default:
				throw new IllegalArgumentException("Unexpected value: " + ruleName);
			}
		}
	}

	private static boolean hasRelevantContext(TripleRule rule, boolean SRC, boolean CORR, boolean TRG) {
		return hasRelevantNodeContext(rule, SRC, TRG)//
				|| hasRelevantRelContext(rule, SRC, TRG)//
				|| hasCorrContext(rule, CORR);
	}

	private static boolean hasRelevantRelContext(TripleRule rule, boolean SRC, boolean TRG) {
		var hasRelevantRelContext = false;
		if (SRC)
			hasRelevantRelContext = rule.getSrcNodeBlocks().stream()//
					.flatMap(n -> n.getRelations().stream())//
					.anyMatch(r -> r.getAction() == null);

		if (TRG)
			hasRelevantRelContext = hasRelevantRelContext || rule.getTrgNodeBlocks().stream()//
					.flatMap(n -> n.getRelations().stream())//
					.anyMatch(r -> r.getAction() == null);

		return hasRelevantRelContext;
	}

	private static boolean hasRelevantNodeContext(TripleRule rule, boolean SRC, boolean TRG) {
		var hasRelevantNodeContext = false;
		if (SRC)
			hasRelevantNodeContext = rule.getSrcNodeBlocks().stream()//
					.anyMatch(n -> n.getAction() == null);

		if (TRG)
			hasRelevantNodeContext = hasRelevantNodeContext || rule.getTrgNodeBlocks().stream()//
					.anyMatch(n -> n.getAction() == null);

		return hasRelevantNodeContext;
	}

	private static boolean hasCorrContext(TripleRule rule, boolean CORR) {
		return CORR && rule.getCorrespondences().stream()//
				.anyMatch(r -> r.getAction() == null);
	}

	private static Optional<TripleRule> toTripleRule(String ruleName, Collection<TripleRule> tripleRules) {
		return tripleRules.stream()//
				.filter(tr -> tr.getName().equals(ruleName))//
				.findAny();
	}
}
