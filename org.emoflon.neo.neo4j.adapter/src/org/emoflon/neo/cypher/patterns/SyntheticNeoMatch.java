package org.emoflon.neo.cypher.patterns;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to represent an empty match, or to construct a match
 * without consulting the pattern matcher. This is sometimes useful when all the
 * information for a match is already available and the next step in a pipeline
 * expects a {@link NeoMatch}.
 * 
 * @author anthonyanjorin
 */
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
