package org.emoflon.neo.engine.api.patterns;

import java.util.Collections;
import java.util.Map;

public class EmptyMask implements IMask {

	@Override
	public Map<String, Long> getMaskedNodes() {
		return Collections.emptyMap();
	}

	@Override
	public Map<String, Object> getMaskedAttributes() {
		return Collections.emptyMap();
	}

	@Override
	public Map<String, Object> getParameters() {
		return Collections.emptyMap();
	}

	@Override
	public void maskNode(String node, Long value) {

	}

	@Override
	public void maskAttribute(String attribute, Object value) {

	}

	@Override
	public void addParameter(String parameter, Object value) {

	}

}
