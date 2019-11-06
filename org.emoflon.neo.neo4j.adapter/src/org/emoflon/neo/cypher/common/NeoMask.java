package org.emoflon.neo.cypher.common;

import java.util.HashMap;
import java.util.Map;

import org.emoflon.neo.engine.api.patterns.IMask;

public class NeoMask implements IMask {
	protected Map<String, Object> parameters;
	protected Map<String, Object> attributeMask;
	protected HashMap<String, Long> nodeMask;

	public NeoMask() {
		parameters = new HashMap<>();
		attributeMask = new HashMap<>();
		nodeMask = new HashMap<>();
	}

	@Override
	public Map<String, Object> getParameters() {
		return parameters;
	}

	@Override
	public Map<String, Long> getMaskedNodes() {
		return nodeMask;
	}

	@Override
	public Map<String, Object> getMaskedAttributes() {
		return attributeMask;
	}

	@Override
	public void maskNode(String node, Long value) {
		nodeMask.put(node, value);
	}

	@Override
	public void maskAttribute(String attribute, Object value) {
		attributeMask.put(attribute, value);
	}

	@Override
	public void addParameter(String parameter, Object value) {
		parameters.put(parameter, value);
	}
}
