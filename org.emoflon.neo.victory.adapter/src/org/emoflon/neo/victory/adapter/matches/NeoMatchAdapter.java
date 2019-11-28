package org.emoflon.neo.victory.adapter.matches;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.victory.adapter.rules.NeoRuleAdapter;
import org.emoflon.victory.ui.api.Graph;
import org.emoflon.victory.ui.api.Match;
import org.emoflon.victory.ui.api.Rule;
import org.emoflon.victory.ui.api.enums.EdgeType;
import org.emoflon.victory.ui.api.impl.GraphBuilder;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;

//TODO To be refactored
public class NeoMatchAdapter implements Match {
	private NeoMatch match;
	private Collection<NeoRuleAdapter> rules;
	private NeoCoreBuilder builder;
	private Map<Integer, Graph> graphs = new HashMap<>();

	public NeoMatchAdapter(NeoCoreBuilder builder, NeoMatch match, Collection<NeoRuleAdapter> rules) {
		this.match = match;
		this.rules = rules;
		this.builder = builder;
	}

	public IMatch getWrappedMatch() {
		return match;
	}

	@Override
	public String getName() {
		return match.getPattern().getName();
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
		return rules.stream().filter(r -> r.getName().equals(getName())).findAny().get();
	}

	@Override
	public Graph getGraph(int pNeighbourhoodSize) {
		if (!graphs.containsKey(pNeighbourhoodSize))
			buildGraph(pNeighbourhoodSize);
		return graphs.get(pNeighbourhoodSize);
	}

	private void buildGraph(int neighbourhoodSize) {

		GraphBuilder graphBuilder = new GraphBuilder();
		Map<Long, NeoModelNodeAdapter> nodeToNeoNode = new HashMap<>();
		Map<Long, NeoModelEdgeAdapter> relations = new HashMap<>();

		if (!(match.getKeysForElements().size() == 0)) {

			var result = builder.executeQuery(MatchQuery.create(match, getRule(), neighbourhoodSize));

			var records = result.list();
			for (var rec : records) {

				var map = rec.asMap();
				for (var o : map.values()) {
					var path = (Path) o;

					path.nodes().forEach(n -> nodeToNeoNode.putIfAbsent(n.id(), new NeoModelNodeAdapter(n)));

					if (neighbourhoodSize > 0) {
						path.relationships().forEach(r -> {
							if (r.hasType("corr")) {

								relations.putIfAbsent(r.id(),
										new NeoModelEdgeAdapter(nodeToNeoNode.get(r.startNodeId()),
												nodeToNeoNode.get(r.endNodeId()), EdgeType.CORR,
												r.asMap().values().toString()));
							} else {
								relations.putIfAbsent(r.id(),
										new NeoModelEdgeAdapter(nodeToNeoNode.get(r.startNodeId()),
												nodeToNeoNode.get(r.endNodeId()), EdgeType.NORMAL, r.type()));
							}
						});
					}
				}
			}

			// populate edges when neighbourhoodSize=0
			if (neighbourhoodSize == 0) {
				for (var id : match.getKeysForElements()) {
					var matchEdges = builder.executeQuery(MatchQuery.getMatchEdges(match.getElement(id)));
					matchEdges.list().forEach(n -> {
						for (var val : n.asMap().values()) {
							var edge = (Relationship) val;

							if (edge.hasType("corr")) {
								relations.putIfAbsent(edge.id(),
										new NeoModelEdgeAdapter(nodeToNeoNode.get(edge.endNodeId()),
												nodeToNeoNode.get(edge.startNodeId()), EdgeType.CORR,
												edge.asMap().values().toString()));
							} else
								relations.putIfAbsent(edge.id(),
										new NeoModelEdgeAdapter(nodeToNeoNode.get(edge.endNodeId()),
												nodeToNeoNode.get(edge.startNodeId()), EdgeType.NORMAL, edge.type()));
						}
					});
				}
				;
			}

		}

		nodeToNeoNode.forEach((Node, neoNode) -> graphBuilder.addNode(neoNode));
		relations.forEach((id, relation) -> graphBuilder.addEdge(relation));
		addMatchTypeEdge(graphBuilder, nodeToNeoNode);
		graphs.put(neighbourhoodSize, graphBuilder.build());

	}

	// Adding MATCH edges between rule and match nodes
	private void addMatchTypeEdge(GraphBuilder graphBuilder, Map<Long, NeoModelNodeAdapter> nodeToNeoNode) {
		for (var id : match.getKeysForElements()) {
			var nodes = getRule().getGraph().getNodes();
			var ruleNode = nodes.stream().filter(n -> n.getName().equals(id)).findFirst();
			ruleNode.ifPresent(rn -> graphBuilder
					.addEdge(new NeoModelEdgeAdapter(rn, nodeToNeoNode.get(match.getElement(id)), EdgeType.MATCH, "")));
		}
		;
	}

}
