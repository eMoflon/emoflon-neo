package org.emoflon.neo.victory.adapter;

import org.emoflon.ibex.tgg.ui.debug.api.Edge;
import org.emoflon.ibex.tgg.ui.debug.api.Node;

import org.emoflon.ibex.tgg.ui.debug.api.enums.Action;
import org.emoflon.ibex.tgg.ui.debug.api.enums.EdgeType;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement;

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
	this.action= Action.CONTEXT;
}

    @Override
    public String getLabel() {
	// TODO Auto-generated method stub
	return name;
    }

    @Override
    public Node getSrcNode() {
	// TODO Auto-generated method stub
	return trg;
    }

    @Override
    public Node getTrgNode() {
	// TODO Auto-generated method stub
	return src;
    }

    @Override
    public EdgeType getType() {
	// TODO Auto-generated method stub
	return type;
    }

    @Override
    public Action getAction() {
	// TODO Auto-generated method stub
	return action;
    }

}
