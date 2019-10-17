package org.emoflon.neo.victory.adapter;

import java.util.List;
import java.util.stream.Collectors;

import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.victory.ui.api.Node;
import org.emoflon.victory.ui.api.enums.Action;
import org.emoflon.victory.ui.api.enums.Domain;

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
