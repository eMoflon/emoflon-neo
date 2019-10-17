package org.emoflon.neo.victory.adapter;

import java.util.HashMap;
import java.util.Map;

import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.TripleRule;
import org.emoflon.neo.neo4j.adapter.models.NeoCoreBootstrapper;
import org.emoflon.neo.neo4j.adapter.models.NeoCoreBuilder;
import org.emoflon.victory.ui.api.Graph;
import org.emoflon.victory.ui.api.Node;
import org.emoflon.victory.ui.api.Rule;
import org.emoflon.victory.ui.api.enums.Action;
import org.emoflon.victory.ui.api.enums.Domain;
import org.emoflon.victory.ui.api.enums.EdgeType;
import org.emoflon.victory.ui.api.impl.GraphBuilder;

public class NeoRuleAdapter implements Rule {
	private org.emoflon.neo.emsl.eMSL.Rule operationalRule;
	private TripleRule tripleRule;
	private GraphBuilder graphBuilder;
	private Map<ModelNodeBlock, Node> blocksToNode;

	public NeoRuleAdapter(org.emoflon.neo.emsl.eMSL.Rule operationalRule, TripleRule tripleRule) {
		this.operationalRule = operationalRule;
		this.tripleRule = tripleRule;
		this.graphBuilder = new GraphBuilder();
		blocksToNode = new HashMap<>();

		createNodes();
		createEdges();
	}

	private void createEdges() {
		for (var n : operationalRule.getNodeBlocks()) {
			for (var r : n.getRelations()) {
				var srcNode = blocksToNode.get(n);
				var trgNode = blocksToNode.get(r.getTarget());
				var type = EdgeType.NORMAL;
				if (r.getTypes().get(0).getType().getName().equals(NeoCoreBootstrapper.CORR)) {
					type = EdgeType.CORR;
				}
				graphBuilder.addEdge(new NeoEdgeAdapter(srcNode, trgNode, type, r));
			}
		}
	}

	private void createNodes() {
		for (var n : operationalRule.getNodeBlocks()) {
			boolean isSrc = tripleRule.getSrcNodeBlocks().stream()//
					.map(nb -> nb.getName())//
					.anyMatch(nb -> nb.equals(n.getName()));
			var domain = isSrc ? Domain.SRC : Domain.TRG;

			var action = Action.CREATE;
			if (n.getAction() == null) {
				if (n.getProperties().stream()
						.anyMatch(p -> p.getType().getName().equals(NeoCoreBuilder.TRANSLATION_MARKER))) {
					action = Action.TRANSLATE;
				} else {
					action = Action.CONTEXT;
				}
			}

			var node = new NeoNodeAdapter(n, domain, action);
			blocksToNode.put(n, node);
			graphBuilder.addNode(node);
		}
	}

	@Override
	public String getName() {
		return operationalRule.getName();
	}

	@Override
	public Graph getGraph() {
		return graphBuilder.build();
	}
}
