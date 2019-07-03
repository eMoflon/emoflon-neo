package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.engine.api.rules.IMatch;

public class NeoConstraintReference {

	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	private NeoCoreBuilder builder;

	private AtomicPattern ap;
	private String name;
	private List<NeoNode> nodes;

	public NeoConstraintReference(AtomicPattern ap, NeoCoreBuilder builder) {
		this.builder = builder;
		this.ap = ap;
		this.name = ap.getName();
		nodes = new ArrayList<>();
		extractNodesAndRelations();
	}
	
	private void extractNodesAndRelations() {
		for (var n : ap.getNodeBlocks()) {
			var node = new NeoNode(n.getType().getName(), n.getName());
			n.getProperties().forEach(p -> node.addProperty(//
					p.getType().getName(), //
					NeoUtil.handleValue(p.getValue())));
			n.getRelations().forEach(r -> node.addRelation(new NeoRelation(//
					node, //
					n.getRelations().indexOf(r), //
					r.getType().getName(), //
					r.getProperties(), //
					r.getTarget().getType().getName(), //
					r.getTarget().getName())));
			nodes.add(node);
		}
	}

	public String getName() {
		return name;
	}

	public AtomicPattern getPattern() {
		return ap;
	}

	public boolean isSatisfied() {

		if (getMatch() != null)
			return true;
		else
			return false;

	}

	public IMatch getMatch() {

		logger.info("Searching matches for Pattern: " + ap.getName());

		var cypherQuery = CypherPatternBuilder.readQuery(nodes, true);
		logger.debug(cypherQuery);

		var result = builder.executeQuery(cypherQuery);

		while (result.hasNext()) {
			var record = result.next();
			logger.info("Found match(es).");
			return new NeoMatch(null, record);
		}
		logger.info("Not matches found.");
		return null;

	}

}
