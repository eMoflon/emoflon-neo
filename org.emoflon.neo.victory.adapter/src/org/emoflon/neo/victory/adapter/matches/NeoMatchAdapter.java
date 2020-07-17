package org.emoflon.neo.victory.adapter.matches;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.neocore.util.NeoCoreConstants;
import org.emoflon.neo.victory.adapter.rules.NeoRuleAdapter;
import org.emoflon.victory.ui.api.Graph;
import org.emoflon.victory.ui.api.Match;
import org.emoflon.victory.ui.api.Rule;
import org.emoflon.victory.ui.api.enums.Domain;
import org.emoflon.victory.ui.api.enums.EdgeType;
import org.emoflon.victory.ui.api.impl.GraphBuilder;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

public class NeoMatchAdapter implements Match {
	private NeoMatch match;
	private Rule rule;
	private String name;
	private NeoCoreBuilder builder;
	private Map<Integer, Graph> graphs;
	private String srcModel;
	private String trgModel;

	public NeoMatchAdapter(NeoCoreBuilder builder, NeoMatch match, Collection<NeoRuleAdapter> rules, String srcModel,
			String trgModel) {
		this.match = match;
		this.builder = builder;
		this.srcModel = srcModel;
		this.trgModel = trgModel;

		graphs = new HashMap<>();
		name = match.getPattern().getName();

		rule = rules.stream()//
				.filter(r -> r.getName().equals(getName()))//
				.findAny()//
				.orElseThrow(() -> new IllegalArgumentException("Can't find " + name + " in " + rules));
	}

	private void buildGraph(int neighbourhoodSize) {
		GraphBuilder graphBuilder = new GraphBuilder();
		Map<Long, NeoModelNodeAdapter> nodeToNeoNode = new HashMap<>();
		Map<Long, NeoModelEdgeAdapter> relations = new HashMap<>();

		var srcElements = builder.getAllElementsOfModel(srcModel);
		var trgElements = builder.getAllElementsOfModel(trgModel);
		var allCorrs = builder.getAllCorrs(srcModel, trgModel);
		var allModelElements = new ArrayList<Long>(srcElements);
		allModelElements.addAll(trgElements);
		allModelElements.addAll(allCorrs);

		var nodes = match.getKeysForElements().stream()//
				.filter(k -> match.getPattern().getContextNodeLabels().contains(k))//
				.map(k -> match.getElement(k))//
				.filter(x -> allModelElements.contains(x))//
				.collect(Collectors.toSet());
		var edges = match.getKeysForElements().stream()//
				.filter(k -> match.getPattern().getContextRelLabels().contains(k))//
				.map(k -> match.getElement(k))//
				.filter(x -> allModelElements.contains(-1 * x))//
				.collect(Collectors.toSet());

		switch (neighbourhoodSize) {
		case 0:
			handleNeighbourhoodSize_0(nodes, edges, nodeToNeoNode, relations, srcElements);
			break;
		case 1:
			handleNeighbourhoodSize_1(nodes, nodeToNeoNode, relations, srcElements);
			break;
		default:
			handleNeighbourhoodSize_2(nodes, nodeToNeoNode, relations, srcElements, allModelElements);
		}

		filterNodes(nodeToNeoNode, allModelElements).forEach(graphBuilder::addNode);
		filterRels(relations, allModelElements).forEach(graphBuilder::addEdge);
		addMatchTypeEdges(graphBuilder, nodeToNeoNode);

		graphs.put(neighbourhoodSize, graphBuilder.build());
	}

	private void handleNeighbourhoodSize_2(Set<Long> nodes, Map<Long, NeoModelNodeAdapter> nodeToNeoNode,
			Map<Long, NeoModelEdgeAdapter> relations, Collection<Long> srcElements, ArrayList<Long> allModelElements) {
		var nodesWithNeighbourhood = new HashSet<>(nodes);
		Map<String, Object> params = Map.of("nodes", nodes);
		var extraNodes = builder.executeQuery(MatchQuery.getNeighbouringNodes("nodes", 1), params);
		for (var rec : extraNodes) {
			for (var node : rec.asMap().values()) {
				var id = (Long) node;
				nodesWithNeighbourhood.add(id);
			}
		}

		handleNeighbourhoodSize_1(nodesWithNeighbourhood, nodeToNeoNode, relations, srcElements);
	}

	private void handleNeighbourhoodSize_1(Set<Long> nodes, Map<Long, NeoModelNodeAdapter> nodeToNeoNode,
			Map<Long, NeoModelEdgeAdapter> relations, Collection<Long> srcElements) {
		Map<String, Object> parameters = Map.of("nodes", nodes);
		var result = builder.executeQuery(MatchQuery.getAllEdges("nodes"), parameters);
		var allEdgesInMatch = result.stream()//
				.flatMap(rec -> rec.asMap().values().stream())//
				.map(v -> (Long) v)//
				.collect(Collectors.toSet());
		handleNeighbourhoodSize_0(nodes, allEdgesInMatch, nodeToNeoNode, relations, srcElements);
	}

	private void handleNeighbourhoodSize_0(Set<Long> nodes, Set<Long> edges,
			Map<Long, NeoModelNodeAdapter> nodeToNeoNode, Map<Long, NeoModelEdgeAdapter> relations,
			Collection<Long> srcElements) {
		if (nodes.size() > 0) {
			Map<String, Object> parameters = Map.of("nodes", nodes);
			var matchNodes = builder.executeQuery(MatchQuery.getMatchNodes("nodes"), parameters);
			for (var rec : matchNodes) {
				for (var node : rec.asMap().values()) {
					var n = (Node) node;
					var domain = srcElements.contains(n.id()) ? Domain.SRC : Domain.TRG;
					nodeToNeoNode.putIfAbsent(n.id(), new NeoModelNodeAdapter(n, domain));
				}
			}

			if (edges.size() > 0) {
				parameters = Map.of("edges", edges);
				var matchEdges = builder.executeQuery(MatchQuery.getMatchEdges("edges"), parameters);
				matchEdges.forEach(n -> {
					for (var val : n.asMap().values())
						adaptRelation(nodeToNeoNode, relations, (Relationship) val);
				});
			}
		}

	}

	private Stream<NeoModelEdgeAdapter> filterRels(Map<Long, NeoModelEdgeAdapter> edges, Collection<Long> allElements) {
		return edges.keySet().stream()//
				.filter(k -> allElements.contains(-1 * k))//
				.map(edges::get);
	}

	private Stream<NeoModelNodeAdapter> filterNodes(Map<Long, NeoModelNodeAdapter> nodes,
			Collection<Long> allElements) {
		return nodes.keySet().stream()//
				.filter(k -> allElements.contains(k))//
				.map(nodes::get);
	}

	private void adaptRelation(//
			Map<Long, NeoModelNodeAdapter> nodeToNeoNode, //
			Map<Long, NeoModelEdgeAdapter> relations, //
			Relationship r) {
		if (r.hasType(NeoCoreConstants.CORR)) {
			relations.putIfAbsent(r.id(), new NeoModelEdgeAdapter(//
					nodeToNeoNode.get(r.startNodeId()), //
					nodeToNeoNode.get(r.endNodeId()), //
					EdgeType.CORR, //
					getTypeOfCorr(r)));
		} else {
			relations.putIfAbsent(r.id(), new NeoModelEdgeAdapter(//
					nodeToNeoNode.get(r.startNodeId()), //
					nodeToNeoNode.get(r.endNodeId()), //
					EdgeType.NORMAL, //
					r.type()));
		}
	}

	private String getTypeOfCorr(Relationship r) {
		return (String) r.asMap().get(NeoCoreConstants._TYPE_PROP);
	}

	private void addMatchTypeEdges(GraphBuilder graphBuilder, Map<Long, NeoModelNodeAdapter> nodeToNeoNode) {
		for (var varName : match.getKeysForElements()) {
			var nodes = getRule().getGraph().getNodes();
			var ruleNode = nodes.stream().filter(n -> n.getName().equals(varName)).findFirst();
			ruleNode.ifPresent(rn -> graphBuilder.addEdge(new NeoModelEdgeAdapter(//
					rn, nodeToNeoNode.get(match.getElement(varName)), EdgeType.MATCH, "")));
		}
	}

	/* Getter */

	public IMatch getWrappedMatch() {
		return match;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isBlocked() {
		// TODO[Victory]
		return false;
	}

	@Override
	public String getBlockingReason() {
		// TODO[Victory]
		return "";
	}

	@Override
	public Rule getRule() {
		return rule;
	}

	@Override
	public Graph getGraph(int pNeighbourhoodSize) {
		if (!graphs.containsKey(pNeighbourhoodSize))
			buildGraph(pNeighbourhoodSize);
		return graphs.get(pNeighbourhoodSize);
	}
}
