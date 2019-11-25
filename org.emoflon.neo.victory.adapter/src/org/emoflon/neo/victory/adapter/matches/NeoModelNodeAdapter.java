package org.emoflon.neo.victory.adapter.matches;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.emoflon.neo.victory.adapter.common.NeoNodeAdapter;
import org.emoflon.victory.ui.api.enums.Action;
import org.emoflon.victory.ui.api.enums.Domain;
import org.neo4j.driver.v1.types.Node;

public class NeoModelNodeAdapter extends NeoNodeAdapter {
	public NeoModelNodeAdapter(Node n) {
		//FIXME:  Determine domain
		super(Domain.SRC, Action.CONTEXT, computeAttributes(n), computeType(n), computeName(n));

		attributes = Arrays.asList(n.labels().toString());
	}

	private static List<String> computeAttributes(Node n) {
		return n.asMap().entrySet().stream()//
				.map(entry -> entry.getKey() + " : " + entry.getValue().toString())//
				.collect(Collectors.toList());
	}

	private static String computeName(Node n) {
		return n.asMap().get("ename").toString() + "_" + n.id();
	}

	private static String computeType(Node n) {
		return n.labels().toString();
	}

}
