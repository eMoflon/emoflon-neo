package org.emoflon.neo.victory.adapter.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.emoflon.victory.ui.api.Node;
import org.emoflon.victory.ui.api.enums.Action;
import org.emoflon.victory.ui.api.enums.Domain;

public abstract class NeoNodeAdapter implements Node {
	protected Domain domain;
	protected Action action;
	protected List<String> attributes;
	protected String type;
	protected String name;

	protected NeoNodeAdapter(Domain domain, Action action, List<String> attributes, String type, String name) {
		Validate.notNull(domain);
		Validate.notNull(action);
		Validate.notNull(attributes);
		Validate.noNullElements(attributes);
		Validate.notNull(type);
		Validate.notNull(name);

		this.domain = domain;
		this.action = action;
		this.attributes = new ArrayList<>(attributes);
		this.type = type;
		this.name = name;
	}

	@Override
	public String toString() {
		return name + ":" + type;
	}

	/* Getters */

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