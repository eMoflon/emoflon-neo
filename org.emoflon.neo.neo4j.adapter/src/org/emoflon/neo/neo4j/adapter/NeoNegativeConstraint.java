package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.engine.api.constraints.INegativeConstraint;
import org.emoflon.neo.engine.api.rules.IMatch;

public class NeoNegativeConstraint implements INegativeConstraint {

	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	private NeoCoreBuilder builder;
	private NeoHelper helper;

	private AtomicPattern ap;
	private String name;
	private List<NeoNode> nodes;
	
	private boolean injective;
	private int uuid;

	public NeoNegativeConstraint(AtomicPattern ap, NeoCoreBuilder builder, boolean injective, NeoHelper helper) {
		this.uuid = helper.addConstraint();
		this.builder = builder;
		this.helper = helper;
		this.ap = ap;
		this.name = ap.getName();
		nodes = new ArrayList<>();
		this.injective = injective;
		extractNodesAndRelations();
	}

	public String getName() {
		return name;
	}

	public AtomicPattern getPattern() {
		return ap;
	}
	
	private void extractNodesAndRelations() {
		
		for (var n : ap.getNodeBlocks()) {
			
			var node = new NeoNode(n.getType().getName(), helper.newConstraintNode(n.getName(), ap, uuid));
			
			n.getProperties().forEach(p -> node.addProperty(//
					p.getType().getName(), //
					NeoUtil.handleValue(p.getValue())));
			
			n.getRelations().forEach(r -> node.addRelation(new NeoRelation(//
					node, //
					helper.newConstraintReference(node.getVarName(), n.getRelations().indexOf(r), r.getType().getName(), r.getTarget().getName(), ap, uuid),
					r.getType().getName(), //
					r.getProperties(), //
					r.getTarget().getType().getName(), //
					helper.newConstraintNode(r.getTarget().getName(), ap, uuid))));
			
			nodes.add(node);
		}
	}
	
	public Collection<NeoNode> getNodes() {
		return nodes;
	}
	
	public String getQueryString_MatchConstraint() { 
		var query = "\nOPTIONAL " + CypherPatternBuilder.matchQuery(nodes);
		if(injective) {
			query += CypherPatternBuilder.injectivityBlock(nodes);
		}
		query += "\n" + CypherPatternBuilder.withCountQuery(nodes,uuid);
		return query + "\n";	
	}
	public String getQueryString_MatchCondition() { 
		var query = "\nOPTIONAL " + CypherPatternBuilder.matchQuery(nodes);
		if(injective) {
			query += CypherPatternBuilder.injectivityBlock(nodes);
		}
		return query + "\n";	
	}
	
	public String getQueryString_WhereConstraint() {
		return CypherPatternBuilder.whereNegativeConstraintQuery(uuid);
	}
	public String getQueryString_WhereConditon() {
		return CypherPatternBuilder.whereNegativeConditionQuery(nodes);
	}

	@Override
	public boolean isSatisfied() {

		if (getViolations() == null)
			return true;
		else
			return false;

	}

	@Override
	public Collection<IMatch> getViolations() {
		logger.info("Check constraint: FORBID " + ap.getName());

		var cypherQuery = CypherPatternBuilder.readQuery(nodes, injective);
		logger.debug(cypherQuery);

		var result = builder.executeQuery(cypherQuery);

		var matches = new ArrayList<IMatch>();
		while (result.hasNext()) {
			matches.add(new NeoMatch(null, result.next()));
		}

		if (!matches.isEmpty()) {
			logger.info("Found match(es). Constraint: FORBID " + ap.getName() + " is NOT complied!");
			return matches;
		}
		
		logger.info("Not matches found. Constraint: FORBID " + ap.getName() + " is complied!");
		return null;
	}

}
