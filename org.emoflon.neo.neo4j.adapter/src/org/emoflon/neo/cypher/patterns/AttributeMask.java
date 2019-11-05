package org.emoflon.neo.cypher.patterns;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AttributeMask extends NeoMask {

	private Map<String, Object> attributeMask = new HashMap<>();

	public void maskAttribute(String attribute, Object value) {
		attributeMask.put(attribute, value);
	}

	@Override
	public Map<String, Long> getMaskedNodes() {
		return Collections.emptyMap();
	}

	@Override
	public Map<String, Object> getMaskedAttributes() {
		return attributeMask;
	}

}
