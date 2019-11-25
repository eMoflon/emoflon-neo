package org.emoflon.neo.victory.adapter.matches;

import org.emoflon.neo.victory.adapter.common.NeoEdgeAdapter;
import org.emoflon.victory.ui.api.Node;
import org.emoflon.victory.ui.api.enums.Action;
import org.emoflon.victory.ui.api.enums.EdgeType;

public class NeoModelEdgeAdapter extends NeoEdgeAdapter {
	public NeoModelEdgeAdapter(Node src, Node trg, EdgeType type, String label) {
		this.src = src;
		this.trg = trg;
		this.type = type;
		this.label = label;
		
		// TODO[Victory] logically assign action to model element
		this.action = Action.CONTEXT;
	}
}
