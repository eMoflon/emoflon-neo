package org.emoflon.neo.engine.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ElementRange {

	private Map<String, List<Long>> typeToIDs;

	public ElementRange() {
		typeToIDs = new HashMap<>();
	}

	public ElementRange(Map<String, List<Long>> typeToIDs) {
		this.typeToIDs = new HashMap<>(typeToIDs);
	}

	public void addIDs(Stream<String> types, Long id) {
		types.forEach(type -> {
			addID(type, id);
		});
	}

	public void addID(String type, Long id) {
		typeToIDs.putIfAbsent(type, new ArrayList<>());
		typeToIDs.get(type).add(id);
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

	public int size() {
		return typeToIDs.keySet().size();
	}

	public ElementRange remove(Collection<Long> ids) {
		var range = new ElementRange();
		typeToIDs.entrySet().stream()//
				.forEach(entry -> {
					entry.getValue().forEach(id -> {
						if (!ids.contains(id)) {
							range.addID(entry.getKey(), id);
						}
					});
				});
		return range;
	}

	public Collection<Long> getIDs() {
		return typeToIDs.values().stream()//
				.flatMap(ids -> ids.stream())//
				.collect(Collectors.toSet());
	}
}
