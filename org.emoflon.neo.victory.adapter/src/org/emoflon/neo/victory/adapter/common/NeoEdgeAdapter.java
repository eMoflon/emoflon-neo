package org.emoflon.neo.victory.adapter.common;

import org.apache.commons.lang.Validate;
import org.emoflon.victory.ui.api.Edge;
import org.emoflon.victory.ui.api.Node;
import org.emoflon.victory.ui.api.enums.Action;
import org.emoflon.victory.ui.api.enums.EdgeType;

public abstract class NeoEdgeAdapter implements Edge {
	protected Node src;
	protected Node trg;
	protected EdgeType type;

	protected String label;
	protected Action action;

	protected NeoEdgeAdapter(Node src, Node trg, EdgeType type, String label, Action action) {
		Validate.notNull(src);
		Validate.notNull(trg);
		Validate.notNull(type);
		Validate.notNull(label);
		Validate.notNull(action);
		
		this.src = src;
		this.trg = trg;
		this.type = type;
		this.label = label;
		this.action = action;
	}

	@Override
	public String toString() {
		return src + "-[ " + label + " ]->" + trg;
	}

	/* Getters */

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public Node getSrcNode() {
		return src;
	}

	@Override
	public Node getTrgNode() {
		return trg;
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
