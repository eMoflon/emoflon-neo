package org.emoflon.neo.victory.adapter.matches;

import org.emoflon.neo.victory.adapter.common.NeoEdgeAdapter;
import org.emoflon.victory.ui.api.Node;
import org.emoflon.victory.ui.api.enums.Action;
import org.emoflon.victory.ui.api.enums.EdgeType;

public class NeoModelEdgeAdapter extends NeoEdgeAdapter {
	public NeoModelEdgeAdapter(Node src, Node trg, EdgeType type, String label) {
		super(src, trg, type, label, Action.CONTEXT);
	}
}
