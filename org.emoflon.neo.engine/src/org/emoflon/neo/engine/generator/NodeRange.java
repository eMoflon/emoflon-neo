package org.emoflon.neo.engine.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class NodeRange {

	private Map<String, List<Long>> typeToIDs;

	public NodeRange() {
		typeToIDs = new HashMap<>();
	}

	public NodeRange(Map<String, List<Long>> typeToIDs) {
		this.typeToIDs = new HashMap<>(typeToIDs);
	}

	public void addIDs(Stream<String> types, Long id) {
		types.forEach(type -> {
			typeToIDs.putIfAbsent(type, new ArrayList<>());
			typeToIDs.get(type).add(id);
		});
	}

	public Collection<Long> sampleIDs(String type, int sampleSize) {
		var idsForType = typeToIDs.getOrDefault(type, Collections.emptyList());

		if (idsForType.size() < sampleSize)
			return idsForType;

		var sampleIDs = new HashSet<Long>();
		while (sampleIDs.size() < sampleSize) {
			var randomIndex = (int) (Math.random() * idsForType.size());
			sampleIDs.add(idsForType.get(randomIndex));
		}

		return sampleIDs;
	}
}
