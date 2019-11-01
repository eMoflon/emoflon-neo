package org.emoflon.neo.victory.adapter;

import org.emoflon.victory.ui.api.Edge;
import org.emoflon.victory.ui.api.Node;
import org.emoflon.victory.ui.api.enums.Action;
import org.emoflon.victory.ui.api.enums.EdgeType;

public class NeoModelEdgeAdapter implements Edge {
	private Node src;
	private Node trg;
	private EdgeType type;
	private String name;
	private Action action;

	public NeoModelEdgeAdapter(Node src, Node trg, EdgeType type, String name) {
		this.src = src;
		this.trg = trg;
		this.type = type;
		this.name = name;
		// TODO[Victory] logically assign action to model element
		this.action = Action.CONTEXT;
	}

	@Override
	public String getLabel() {
		return name;
	}

	@Override
	public Node getSrcNode() {

		return trg;
	}

	@Override
	public Node getTrgNode() {
		return src;
	}

	@Override
	public EdgeType getType() {
		return type;
	}

	@Override
	public Action getAction() {
		return action;
	}

}
