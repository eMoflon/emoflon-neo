package org.emoflon.neo.engine.modules.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.emoflon.neo.emsl.compiler.TGGCompiler;
import org.emoflon.neo.emsl.eMSL.TripleRule;
import org.emoflon.neo.emsl.refinement.EMSLFlattener;
import org.emoflon.neo.emsl.util.FlattenerException;

public class TripleRuleAnalyser {
	private Collection<TripleRule> tripleRules;

	public TripleRuleAnalyser(Collection<TripleRule> tripleRules) {
		this.tripleRules = new ArrayList<>();
		for (var tripleRule : tripleRules) {
			try {
				var flatTr = (TripleRule) EMSLFlattener.flatten(tripleRule);
				this.tripleRules.add(flatTr);
			} catch (FlattenerException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Checks if the provided rule has (i) no corr context elements and, (ii) no
	 * context element of a type in one of the provided relevant metamodels.
	 * 
	 * @param rule
	 * @param relevantMetamodels
	 * @return
	 */
	public boolean hasRelevantContext(String ruleName, boolean SRC, boolean CORR, boolean TRG) {
		var tripleRule = toTripleRule(ruleName);
		if (tripleRule.isPresent()) {
			return hasRelevantContext(tripleRule.get(), SRC, CORR, TRG);
		} else {
			// Triple rule does not exist, so it must have been added by the compiler
			switch (ruleName) {
			case TGGCompiler.CREATE_SRC_MODEL_RULE:
				return SRC;
			case TGGCompiler.CREATE_TRG_MODEL_RULE:
				return TRG;
//			case TGGCompiler.CREATE_MODELS_RULE:
//				return SRC || CORR || TRG;
			default:
				throw new IllegalArgumentException("Unexpected value: " + ruleName);
			}
		}
	}

	private boolean hasRelevantContext(TripleRule rule, boolean SRC, boolean CORR, boolean TRG) {
		return hasRelevantNodeContext(rule, SRC, TRG)//
				|| hasRelevantRelContext(rule, SRC, TRG)//
				|| hasCorrContext(rule, CORR);
	}

	private boolean hasRelevantRelContext(TripleRule rule, boolean SRC, boolean TRG) {
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

	private boolean hasRelevantNodeContext(TripleRule rule, boolean SRC, boolean TRG) {
		var hasRelevantNodeContext = false;
		if (SRC)
			hasRelevantNodeContext = rule.getSrcNodeBlocks().stream()//
					.anyMatch(n -> n.getAction() == null);

		if (TRG)
			hasRelevantNodeContext = hasRelevantNodeContext || rule.getTrgNodeBlocks().stream()//
					.anyMatch(n -> n.getAction() == null);

		return hasRelevantNodeContext;
	}

	private boolean hasCorrContext(TripleRule rule, boolean CORR) {
		return CORR && rule.getCorrespondences().stream()//
				.anyMatch(r -> r.getAction() == null);
	}

	private Optional<TripleRule> toTripleRule(String ruleName) {
		return tripleRules.stream()//
				.filter(tr -> ruleName.startsWith(tr.getName()))//
				.findAny();
	}

	public boolean isNodeInSrcDomain(String ruleName, String nodeName) {
		var tripleRule = toTripleRule(ruleName);
		if (tripleRule.isPresent()) {
			return tripleRule.get().getSrcNodeBlocks().stream()//
					.anyMatch(nb -> nb.getName().equals(nodeName));
		} else {
			return ruleName.equals(TGGCompiler.CREATE_SRC_MODEL_RULE);
//			return ruleName.equals(TGGCompiler.CREATE_MODELS_RULE);
		}
	}
}
