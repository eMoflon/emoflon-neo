package org.emoflon.neo.cypher.patterns;

import java.util.HashMap;
import java.util.Map;

import org.emoflon.neo.cypher.common.NeoNode;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.emsl.eMSL.ConstraintBody;
import org.emoflon.neo.emsl.eMSL.NegativeConstraint;
import org.emoflon.neo.emsl.eMSL.PositiveConstraint;
import org.emoflon.neo.emsl.util.FlattenerException;

public class NeoPredicatePattern extends NeoBasicPattern implements NeoSubPattern {
	private int index;
	private boolean isPositive;
	private Map<NeoNode, NeoNode> boundNodes;

	public NeoPredicatePattern(ConstraintBody constraint, int index, NeoPattern parent) throws FlattenerException {
		super(extractPattern(constraint), false);
		boundNodes = new HashMap<>();
		this.index = index;
		this.isPositive = constraint instanceof PositiveConstraint;

		for (NeoNode n : nodes) {
			parent.nodes.stream()//
					.filter(node -> node.getName().equals(n.getName()))//
					.findAny()//
					.map(node -> boundNodes.put(n, node));
		}

		nodes.forEach(n -> n.setName("_" + index + "_" + n.getName()));
		relations.forEach(r -> r.setName("_" + index + "_" + r.getName()));
	}

	private static AtomicPattern extractPattern(ConstraintBody constraint) {
		if (constraint instanceof PositiveConstraint) {
			return ((PositiveConstraint) constraint).getPattern();
		} else {
			return ((NegativeConstraint) constraint).getPattern();
		}
	}

	public Map<NeoNode, NeoNode> getBoundNodes() {
		return boundNodes;
	}

	public int getIndex() {
		return index;
	}

	public boolean isPositive() {
		return isPositive;
	}

	@Override
	public String getLogicVariable() {
		return "m" + index;
	}
}
