package org.emoflon.neo.cypher.patterns;

import java.util.HashMap;
import java.util.Map;

public class SyntheticNeoMatch extends NeoMatch {
	private Map<String, Long> elements;
	
	public SyntheticNeoMatch(NeoMatch other) {
		super(other);
	}

	public SyntheticNeoMatch(NeoPattern pattern) {
		super(pattern, null);
		elements = new HashMap<>();
	}

	public SyntheticNeoMatch setElement(long id, String element) {
		elements.put(element, id);
		return this;
	}
	
	@Override
	public Map<String, Object> convertToMap() {
		var map = new HashMap<String, Object>(elements.keySet().size());
		map.putAll(parameters);
		map.putAll(elements);
		map.put(getIdParameter(), hashCode());
		return map;
	}
}
