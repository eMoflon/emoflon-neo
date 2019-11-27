package org.emoflon.neo.victory.adapter.rules;

import java.util.stream.Collectors;

import org.emoflon.neo.cypher.models.NeoCoreBootstrapper;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement;
import org.emoflon.neo.emsl.eMSL.PrimitiveString;
import org.emoflon.neo.victory.adapter.common.NeoEdgeAdapter;
import org.emoflon.neo.victory.adapter.common.NeoVictoryUtil;
import org.emoflon.victory.ui.api.Node;
import org.emoflon.victory.ui.api.enums.Action;
import org.emoflon.victory.ui.api.enums.EdgeType;

/**
 * Represents an edge in a rule so it can be visualised by Victory.
 * 
 * @author anthonyanjorin
 */
public class NeoRuleEdgeAdapter extends NeoEdgeAdapter {
	public NeoRuleEdgeAdapter(Node src, Node trg, EdgeType type, ModelRelationStatement relation) {
		super(src, trg, type, computeLabel(type, relation), computeAction(relation));
	}

	private static Action computeAction(ModelRelationStatement relation) {
		return NeoVictoryUtil.computeAction(relation.getAction(), relation.getProperties());
	}

	private static String computeLabel(EdgeType type, ModelRelationStatement relation) {
		if (type.equals(EdgeType.CORR)) {
			return relation.getProperties().stream()//
					.filter(p -> p.getType().getName().equals(NeoCoreBootstrapper._TYPE_PROP))//
					.map(p -> ((PrimitiveString) p.getValue()).getLiteral())//
					.findAny()//
					.orElseThrow(() -> new IllegalArgumentException("Edge is not a corr as it has no type property."));
		}

		// Multiple types are possible as relations can be paths
		var label = relation.getTypes().stream()//
				.map(t -> t.getType().getName())//
				.collect(Collectors.joining("|"));

		return label;
	}
}