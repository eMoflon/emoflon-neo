package org.emoflon.neo.victory.adapter;

import java.util.List;
import java.util.stream.Collectors;

import org.emoflon.ibex.tgg.ui.debug.api.Node;
import org.emoflon.ibex.tgg.ui.debug.api.enums.Action;
import org.emoflon.ibex.tgg.ui.debug.api.enums.Domain;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;

public class NeoNodeAdapter implements Node {
	private ModelNodeBlock node;
	private Domain domain;
	private Action action;

	public NeoNodeAdapter(ModelNodeBlock node, Domain domain, Action action) {
		this.node = node;
		this.domain = domain;
		this.action = action;
	}

	@Override
	public String getType() {
		return node.getType().getName();
	}

	@Override
	public String getName() {
		return node.getName();
	}

	@Override
	public Domain getDomain() {
		return domain;
	}

	@Override
	public Action getAction() {
		return action;
	}

	@Override
	public List<String> getAttributes() {
		return node.getProperties().stream()//
				.map(p -> p.getType().getName() + " " + p.getOp().getName() + " " + p.getValue())//
				.collect(Collectors.toList());
	}
}
