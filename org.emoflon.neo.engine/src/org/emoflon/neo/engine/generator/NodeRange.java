package org.emoflon.neo.engine.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NodeRange {
	private static final String NODE_RANGE_PARAM = "nodeRange";

	private Map<String, List<Long>> typeToIDs;

	public NodeRange() {
		typeToIDs = new HashMap<>();
	}

	public NodeRange(Map<String, List<Long>> typeToIDs) {
		this.typeToIDs = new HashMap<>(typeToIDs);
	}

	public void addIDsForTypes(Stream<String> types, Long id) {
		types.forEach(type -> {
			typeToIDs.putIfAbsent(type, new ArrayList<>());
			typeToIDs.get(type).add(id);
		});
	}

	public NodeRange randomSampling(int sampleSize, Collection<String> restrictedTypes) {
		var reducedTypeToIDs = new HashMap<String, List<Long>>();
		typeToIDs.entrySet().stream()//
				.filter(entry -> restrictedTypes.contains(entry.getKey()))//
				.forEach(entry -> {
					Collections.shuffle(entry.getValue());
					var endPos = Math.min(sampleSize, entry.getValue().size());
					reducedTypeToIDs.put(entry.getKey(), entry.getValue().subList(0, endPos));
				});

		return new NodeRange(reducedTypeToIDs);
	}

	public boolean hasRangeFor(String type) {
		return typeToIDs.containsKey(type);
	}

	public String getParameterFor(String type) {
		return NODE_RANGE_PARAM + "__" + type;
	}

	public Map<String, Object> getParameters() {
		return typeToIDs.entrySet().stream().collect(Collectors.toMap(//
				entry -> getParameterFor(entry.getKey()), //
				entry -> entry.getValue()//
		));
	}
}
