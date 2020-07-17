package org.emoflon.neo.victory.adapter.matches;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.emoflon.neo.victory.adapter.common.NeoNodeAdapter;
import org.emoflon.victory.ui.api.enums.Action;
import org.emoflon.victory.ui.api.enums.Domain;
import org.neo4j.driver.types.Node;

public class NeoModelNodeAdapter extends NeoNodeAdapter {
	public NeoModelNodeAdapter(Node n, Domain domain) {
		super(domain, Action.CONTEXT, computeAttributes(n), computeType(n), computeName(n));
	}

	private static List<String> computeAttributes(Node n) {
		return n.asMap().entrySet().stream()//
				.map(entry -> entry.getKey() + " : " + entry.getValue().toString())//
				.collect(Collectors.toList());
	}

	private static String computeName(Node n) {
		return "o_" + n.id();
	}

	private static String computeType(Node n) {
		var labels = new ArrayList<String>();
		n.labels().iterator().forEachRemaining(labels::add);
		return labels.stream()//
				.filter(l -> !l.startsWith("NeoCore__"))
				.collect(Collectors.joining("_"));
	}

}
