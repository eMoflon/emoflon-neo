package org.emoflon.neo.victory.adapter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.victory.ui.api.Node;
import org.emoflon.victory.ui.api.enums.Action;
import org.emoflon.victory.ui.api.enums.Domain;

public class NeoNodeAdapter implements Node {
	private Domain domain;
	private Action action;
	private List<String> attributes;
	private String type;
	private String name;

	public NeoNodeAdapter(ModelNodeBlock node, Domain domain, Action action) {
		this.domain = domain;
		this.action = action;
		attributes = node.getProperties().stream()//
				.map(p -> p.getType().getName() + " " + p.getOp().getName() + " " + p.getValue())//
				.collect(Collectors.toList());
		type = node.getType().getName();
		name = node.getName();
	}

	public NeoNodeAdapter(org.neo4j.driver.v1.types.Node node) {
		// TODO[Victory] logically assign domain and action to model nodes
		this.domain = Domain.SRC;
		this.action = Action.CONTEXT;

		attributes = Arrays.asList(node.labels().toString());
		name = node.labels().toString() + node.id();
		type = node.asMap().get("ename").toString();
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getName() {
		return name;
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
		return attributes;
	}
}
