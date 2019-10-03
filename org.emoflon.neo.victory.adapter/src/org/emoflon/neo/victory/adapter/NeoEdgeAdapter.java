package org.emoflon.neo.victory.adapter;

import java.util.stream.Collectors;

import org.emoflon.ibex.tgg.ui.debug.api.Edge;
import org.emoflon.ibex.tgg.ui.debug.api.Node;
import org.emoflon.ibex.tgg.ui.debug.api.enums.Action;
import org.emoflon.ibex.tgg.ui.debug.api.enums.EdgeType;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement;
import org.emoflon.neo.emsl.eMSL.PrimitiveString;
import org.emoflon.neo.neo4j.adapter.models.NeoCoreBootstrapper;
import org.emoflon.neo.neo4j.adapter.models.NeoCoreBuilder;

public class NeoEdgeAdapter implements Edge {
	private Node src;
	private Node trg;
	private EdgeType type;
	private ModelRelationStatement relation;

	public NeoEdgeAdapter(Node src, Node trg, EdgeType type, ModelRelationStatement relation) {
		this.src = src;
		this.trg = trg;
		this.type = type;
		this.relation = relation;
	}

	@Override
	public String getLabel() {
		var label = relation.getTypes().stream()//
				.map(t -> t.getType().getName())//
				.collect(Collectors.joining("|"));

		if (type.equals(EdgeType.CORR)) {
			var name = relation.getProperties().stream()//
					.filter(p -> p.getType().getName().equals(NeoCoreBootstrapper._TYPE_PROP))//
					.map(p -> ((PrimitiveString)p.getValue()).getLiteral())//
					.findAny();

			return name.orElse(label);
		}

		return label;
	}

	@Override
	public Node getSrcNode() {
		return src;
	}

	@Override
	public Node getTrgNode() {
		return trg;
	}

	@Override
	public EdgeType getType() {
		return type;
	}

	@Override
	public Action getAction() {
		if (relation.getAction() == null) {
			if (relation.getProperties().stream()//
					.anyMatch(p -> p.getType().getName().equals(NeoCoreBuilder.TRANSLATION_MARKER)))
				return Action.TRANSLATE;
			else
				return Action.CONTEXT;
		}

		return Action.CREATE;
	}
}
