package org.emoflon.neo.victory.adapter.matches;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;

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

		var srcElements = builder.getAllElementsInModel(srcModel);

		var nodes = match.getKeysForElements().stream()//
				.filter(k -> match.getPattern().getContextNodeLabels().contains(k))//
				.map(k -> match.getElement(k))//
				.collect(Collectors.toList());
		var edges = match.getKeysForElements().stream()//
				.filter(k -> match.getPattern().getContextRelLabels().contains(k))//
				.map(k -> match.getElement(k))//
				.collect(Collectors.toList());

		if (nodes.size() > 0) {
			var result = builder.executeQuery(MatchQuery.determineNeighbourhood(nodes, neighbourhoodSize));
			var records = result.list();
			for (var rec : records) {
				var map = rec.asMap();
				for (var o : map.values()) {
					var path = (Path) o;
					path.nodes().forEach(n -> {
						// TODO: if not in src, check trg, if not in trg set to OTHER
						var domain = srcElements.contains(n.id()) ? Domain.SRC : Domain.TRG;
						nodeToNeoNode.putIfAbsent(n.id(), new NeoModelNodeAdapter(n, domain));
					});
					if (neighbourhoodSize > 0)
						extractRelationshipsFromPath(nodeToNeoNode, relations, path);
				}
			}

			if (neighbourhoodSize == 0 && edges.size() > 0) {
				var matchEdges = builder.executeQuery(MatchQuery.getMatchEdges(edges));
				matchEdges.list().forEach(n -> {
					for (var val : n.asMap().values())
						adaptRelation(nodeToNeoNode, relations, (Relationship) val);
				});
			}
		}

		nodeToNeoNode.values().forEach(graphBuilder::addNode);
		relations.values().forEach(graphBuilder::addEdge);
		addMatchTypeEdges(graphBuilder, nodeToNeoNode);

		graphs.put(neighbourhoodSize, graphBuilder.build());
	}

	private void extractRelationshipsFromPath(//
			Map<Long, NeoModelNodeAdapter> nodeToNeoNode, //
			Map<Long, NeoModelEdgeAdapter> relations, //
			Path path) {
		path.relationships().forEach(r -> adaptRelation(nodeToNeoNode, relations, r));
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
