package org.emoflon.neo.emsl.compiler;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import org.emoflon.neo.emsl.eMSL.ActionOperator;
import org.emoflon.neo.emsl.eMSL.Correspondence;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement;
import org.emoflon.neo.emsl.eMSL.TripleRule;

public class TGGCompilerValidations {

	public static ArrayList<String> getGreenElements(TripleRule rule) {
		ArrayList<String> greenElements = new ArrayList<>();

		for (ModelNodeBlock srcNode : rule.getSrcNodeBlocks()) {
			if (srcNode.getAction() != null && ActionOperator.CREATE.equals(srcNode.getAction().getOp())) {
				greenElements.add(srcNode.getName());
			}
			for (ModelRelationStatement rel : srcNode.getRelations().stream()
					.filter(r -> (r.getAction() != null && ActionOperator.CREATE.equals(r.getAction().getOp())))
					.collect(Collectors.toList()))
				greenElements.add(srcNode.getName() + "->" + rel.getTarget().getName());
		}

		for (ModelNodeBlock srcNode : rule.getTrgNodeBlocks()) {
			if (srcNode.getAction() != null && ActionOperator.CREATE.equals(srcNode.getAction().getOp())) {
				greenElements.add(srcNode.getName());
			}
			for (ModelRelationStatement rel : srcNode.getRelations().stream()
					.filter(r -> (r.getAction() != null && ActionOperator.CREATE.equals(r.getAction().getOp())))
					.collect(Collectors.toList()))
				greenElements.add(srcNode.getName() + "->" + rel.getTarget().getName());
		}

		for (Correspondence corr : rule.getCorrespondences().stream()
				.filter(c -> (c.getAction() != null && ActionOperator.CREATE.equals(c.getAction().getOp())))
				.collect(Collectors.toList()))
			greenElements.add(corr.getSource().getName() + "->" + corr.getTarget().getName());

		return greenElements;
	}

	public static boolean isValidRule(Operation op, TripleRule rule, int ruleID, ArrayList<String> greenElements) {
		try {
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
						checkValidity(srcAction, relAction, trgAction);
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
						checkValidity(srcAction, relAction, trgAction);
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
				checkValidity(srcAction, relAction, trgAction);
			}
		} catch (Error e) {
			return false;
		}
		return true;
	}

	private static void checkValidity(String srcAction, String relAction, String trgAction) {
		if (!relAction.equals(ActionOperator.CREATE.toString()) && (srcAction.equals(ActionOperator.CREATE.toString())
				|| trgAction.equals(ActionOperator.CREATE.toString())))
			throw new Error();
	}

	public static int binaryAND(int a, int b) {
		if (a >= 0 && b >= 0)
			return a & b;
		return 0;
	}
}
