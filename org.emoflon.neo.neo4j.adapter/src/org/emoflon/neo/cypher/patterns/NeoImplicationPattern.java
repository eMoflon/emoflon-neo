package org.emoflon.neo.cypher.patterns;

import java.util.HashMap;
import java.util.Map;

import org.emoflon.neo.cypher.common.NeoNode;
import org.emoflon.neo.emsl.eMSL.Implication;
import org.emoflon.neo.emsl.util.FlattenerException;

public class NeoImplicationPattern implements NeoSubPattern {
	private NeoBasicPattern premise;
	private NeoBasicPattern conclusion;
	private int index;
	private String name;
	private Map<NeoNode, NeoNode> boundNodesInPremise;
	private Map<NeoNode, NeoNode> boundNodesInConclusion;

	public NeoImplicationPattern(Implication condition, int index, NeoPattern parent) throws FlattenerException {
		this.index = index;
		this.name = "if " + condition.getPremise().getName() + " then " + condition.getConclusion().getName();

		// Compute premise and conclusion as basic patterns
		premise = new NeoBasicPattern(condition.getPremise());
		conclusion = new NeoBasicPattern(condition.getConclusion());

		// Compute bound nodes for both
		boundNodesInPremise = new HashMap<>();
		for (NeoNode n : premise.nodes) {
			parent.nodes.stream()//
					.filter(node -> node.getName().equals(n.getName()))//
					.findAny()//
					.map(node -> boundNodesInPremise.put(n, node));
		}

		boundNodesInConclusion = new HashMap<>();
		for (NeoNode n : conclusion.nodes) {
			parent.nodes.stream()//
					.filter(node -> node.getName().equals(n.getName()))//
					.findAny()//
					.map(node -> boundNodesInConclusion.put(n, node));

			premise.nodes.stream()//
					.filter(node -> node.getName().equals(n.getName()))//
					.findAny()//
					.map(node -> boundNodesInConclusion.putIfAbsent(n, node));
		}

		premise.nodes.forEach(n -> n.setName("_" + index + "_if_" + n.getName()));
		premise.relations.forEach(r -> r.setName("_" + index + "_if_" + r.getName()));

		conclusion.nodes.forEach(n -> n.setName("_" + index + "_then_" + n.getName()));
		conclusion.relations.forEach(r -> r.setName("_" + index + "_then_" + r.getName()));
	}

	@Override
	public String getLogicVariable() {
		return "m" + index;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public String getName() {
		return name;
	}

	public NeoBasicPattern getPremise() {
		return premise;
	}

	public NeoBasicPattern getConclusion() {
		return conclusion;
	}

	public Map<NeoNode, NeoNode> getBoundNodesInPremise() {
		return boundNodesInPremise;
	}

	public Map<NeoNode, NeoNode> getBoundNodesInConclusion() {
		return boundNodesInConclusion;
	}
}
