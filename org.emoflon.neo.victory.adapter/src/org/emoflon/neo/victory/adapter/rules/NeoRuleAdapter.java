package org.emoflon.neo.victory.adapter.rules;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.Rule;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;
import org.emoflon.neo.neocore.util.NeoCoreConstants;
import org.emoflon.neo.victory.adapter.common.NeoVictoryUtil;
import org.emoflon.victory.ui.api.Graph;
import org.emoflon.victory.ui.api.Node;
import org.emoflon.victory.ui.api.enums.Domain;
import org.emoflon.victory.ui.api.enums.EdgeType;
import org.emoflon.victory.ui.api.impl.GraphBuilder;

/**
 * Wraps a pair of triple rule and operational rule to represent a rule for
 * Victory. Both are needed to discern the correct action (using the operational
 * rule), and correct domain (comparing to the triple rule).
 * 
 * @author anthonyanjorin
 */
public class NeoRuleAdapter implements org.emoflon.victory.ui.api.Rule {
	private String name;
	private Graph graph;
	private TripleRuleAnalyser analyser;

	private Map<ModelNodeBlock, Node> blocksToNode;
	private GraphBuilder graphBuilder;

	public NeoRuleAdapter(Rule opRule, TripleRuleAnalyser analyser) {
		Validate.notNull(opRule);
		Validate.notNull(analyser);

		name = opRule.getName();
		this.analyser = analyser;

		blocksToNode = new HashMap<>();
		graphBuilder = new GraphBuilder();

		createNodes(opRule);
		createEdges(opRule);

		graph = graphBuilder.build();
	}

	private boolean acceptNode(NeoRuleNodeAdapter node) {
		return !node.getType().equals(NeoCoreConstants.MODEL) &&
				!node.getType().equals(NeoCoreConstants.METAMODEL);
	}

	private boolean acceptEdge(NeoRuleEdgeAdapter edge) {
		return !edge.getLabel().equals(NeoCoreConstants.META_EL_OF) &&
				!edge.getLabel().equals(NeoCoreConstants.CONFORMS_TO_PROP);
	}

	private void createNodes(Rule opRule) {
		for (var n : opRule.getNodeBlocks()) {
			var domain = analyser.isNodeInSrcDomain(opRule.getName(), n.getName()) ? Domain.SRC : Domain.TRG;
			var action = NeoVictoryUtil.computeAction(n.getAction(), n.getProperties());
			var node = new NeoRuleNodeAdapter(n, domain, action);

			blocksToNode.put(n, node);
			if (acceptNode(node))
				graphBuilder.addNode(node);
		}
	}

	private void createEdges(Rule opRule) {
		for (var n : opRule.getNodeBlocks()) {
			for (var r : n.getRelations()) {
				var srcNode = blocksToNode.get(n);
				var trgNode = blocksToNode.get(r.getTarget());
				var type = EdgeType.NORMAL;
				if (EMSLUtil.getAllTypes(r).contains(NeoCoreConstants.CORR)) {
					type = EdgeType.CORR;
				}

				var edge = new NeoRuleEdgeAdapter(srcNode, trgNode, type, r);
				if (acceptEdge(edge))
					graphBuilder.addEdge(edge);
			}
		}
	}

	@Override
	public String toString() {
		return name;
	}

	/* Getters */

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Graph getGraph() {
		return graph;
	}
}
