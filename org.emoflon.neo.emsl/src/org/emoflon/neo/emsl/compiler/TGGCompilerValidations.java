package org.emoflon.neo.emsl.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.emoflon.neo.emsl.eMSL.ActionOperator;
import org.emoflon.neo.emsl.eMSL.Correspondence;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement;
import org.emoflon.neo.emsl.eMSL.TripleRule;
import org.emoflon.neo.emsl.compiler.Operation.Domain;

public class TGGCompilerValidations {

	public static Map<Domain, ArrayList<String>> getGreenElements(TripleRule rule) {
		ArrayList<String> srcElements = new ArrayList<>();
		ArrayList<String> trgElements = new ArrayList<>();
		ArrayList<String> corrElements = new ArrayList<>();

		for (ModelNodeBlock srcNode : rule.getSrcNodeBlocks()) {
			if (srcNode.getAction() != null && ActionOperator.CREATE.equals(srcNode.getAction().getOp())) {
				srcElements.add(srcNode.getName());
			}
			for (ModelRelationStatement rel : srcNode.getRelations().stream()
					.filter(r -> (r.getAction() != null && ActionOperator.CREATE.equals(r.getAction().getOp())))
					.collect(Collectors.toList()))
				srcElements.add(srcNode.getName() + "->" + rel.getTarget().getName());
		}

		for (ModelNodeBlock srcNode : rule.getTrgNodeBlocks()) {
			if (srcNode.getAction() != null && ActionOperator.CREATE.equals(srcNode.getAction().getOp())) {
				trgElements.add(srcNode.getName());
			}
			for (ModelRelationStatement rel : srcNode.getRelations().stream()
					.filter(r -> (r.getAction() != null && ActionOperator.CREATE.equals(r.getAction().getOp())))
					.collect(Collectors.toList()))
				trgElements.add(srcNode.getName() + "->" + rel.getTarget().getName());
		}

		for (Correspondence corr : rule.getCorrespondences().stream()
				.filter(c -> (c.getAction() != null && ActionOperator.CREATE.equals(c.getAction().getOp())))
				.collect(Collectors.toList()))
			corrElements.add(corr.getSource().getName() + "->" + corr.getTarget().getName());

		Map<Domain, ArrayList<String>> greenElements = new HashMap<>();
		greenElements.put(Domain.SRC, srcElements);
		greenElements.put(Domain.TRG, trgElements);
		greenElements.put(Domain.CORR, corrElements);
		return greenElements;
	}

	public static boolean isValidRule(Operation op, TripleRule rule, int ruleID,
			Map<Domain, ArrayList<String>> greenElements) {

		// temporary method for allowing only FWD, BWD, CC and CO
		if (!isValidID(ruleID, greenElements))
			return false;

		for (ModelNodeBlock srcNode : rule.getSrcNodeBlocks()) {
			for (ModelRelationStatement rel : srcNode.getRelations()) {
				String srcAction = op.getAction(srcNode.getAction(), ruleID, greenElements, srcNode.getName());
				String relAction = op.getAction(rel.getAction(), ruleID, greenElements,
						srcNode.getName() + "->" + rel.getTarget().getName());
				Optional<ModelNodeBlock> trgNode = rule.getSrcNodeBlocks().stream()
						.filter(b -> (b.getName().equals(rel.getTarget().getName()))).findAny();

				if (trgNode.isPresent()) {
					String trgAction = op.getAction(trgNode.get().getAction(), ruleID, greenElements,
							trgNode.get().getName());
					if (!checkValidity(srcAction, relAction, trgAction)) {
						return false;
					}
				}
			}
		}
		for (ModelNodeBlock srcNode : rule.getTrgNodeBlocks()) {
			for (ModelRelationStatement rel : srcNode.getRelations()) {
				String srcAction = op.getAction(srcNode.getAction(), ruleID, greenElements, srcNode.getName());
				String relAction = op.getAction(rel.getAction(), ruleID, greenElements,
						srcNode.getName() + "->" + rel.getTarget().getName());
				Optional<ModelNodeBlock> trgNode = rule.getTrgNodeBlocks().stream()
						.filter(b -> (b.getName().equals(rel.getTarget().getName()))).findAny();

				if (trgNode.isPresent()) {
					String trgAction = op.getAction(trgNode.get().getAction(), ruleID, greenElements,
							trgNode.get().getName());
					if (!checkValidity(srcAction, relAction, trgAction)) {
						return false;
					}
				}
			}
		}
		for (Correspondence corr : rule.getCorrespondences()) {
			String srcAction = op.getAction(corr.getSource().getAction(), ruleID, greenElements,
					corr.getSource().getName());
			String relAction = op.getAction(corr.getAction(), ruleID, greenElements,
					corr.getSource().getName() + "->" + corr.getTarget().getName());
			String trgAction = op.getAction(corr.getTarget().getAction(), ruleID, greenElements,
					corr.getTarget().getName());
			if (!checkValidity(srcAction, relAction, trgAction)) {
				return false;
			}
		}
		return true;
	}

	// temporary method for allowing only FWD, BWD, CC and CO
	private static boolean isValidID(int ruleID, Map<Domain, ArrayList<String>> greenElements) {
		int s = greenElements.get(Domain.SRC).size(); // number of source elements
		int t = greenElements.get(Domain.TRG).size(); // number of target elements
		int c = greenElements.get(Domain.CORR).size(); // number of correspondences

		int coRuleID = 0;
		int ccRuleID = (1 << c) - 1;
		int fwdRuleID = (((1 << s) - 1) << (t + c)) + (1 << c) - 1;
		int bwdRuleID = (((1 << t) - 1) << c) + (1 << c) - 1;

		return ruleID == coRuleID || ruleID == ccRuleID || ruleID == fwdRuleID || ruleID == bwdRuleID;
	}

	private static boolean checkValidity(String srcAction, String relAction, String trgAction) {
		if (!relAction.equals(ActionOperator.CREATE.toString()) && (srcAction.equals(ActionOperator.CREATE.toString())
				|| trgAction.equals(ActionOperator.CREATE.toString())))
			return false;
		return true;
	}

	public static int binaryAND(int a, int b) {
		if (a >= 0 && b >= 0)
			return a & b;
		return 0;
	}
}
