package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.engine.api.constraints.IPositiveConstraint;
import org.emoflon.neo.engine.api.rules.IMatch;

public class NeoPositiveConstraint implements IPositiveConstraint {

	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	private NeoCoreBuilder builder;
	private NeoHelper helper;

	private AtomicPattern ap;
	private String name;
	private List<NeoNode> nodes;
	
	private boolean injective;
	private int uuid;

	public NeoPositiveConstraint(AtomicPattern ap, NeoCoreBuilder builder, boolean injective, NeoHelper helper) {
		this.uuid = helper.addConstraint();
		this.builder = builder;
		this.helper = helper;
		this.name = ap.getName();
		this.ap = ap;
		this.nodes = new ArrayList<>();
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
					helper.newConstraintReference(node.getVarName(), n.getRelations().indexOf(r), r.getTarget().getName(), ap),
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
		return CypherPatternBuilder.wherePositiveConstraintQuery(uuid);
	}
	public String getQueryString_WhereConditon() {
		return CypherPatternBuilder.wherePositiveConditionQuery(nodes);
	}

	@Override
	public boolean isSatisfied() {

		if (getMatch() != null)
			return true;
		else
			return false;

	}

	@Override
	public IMatch getMatch() {

		logger.info("Check constraint: ENFORCE " + ap.getName());

		var cypherQuery = CypherPatternBuilder.readQuery(nodes, injective);
		logger.debug(cypherQuery);

		var result = builder.executeQuery(cypherQuery);

		if (result.hasNext()) {
			logger.info("Found match(es). Constraint: ENFORCE " + ap.getName() + " is complied!");
			return new NeoMatch(null, result.next());
		}
		logger.info("Not matches found. Constraint: ENFORCE " + ap.getName() + " is NOT complied!");
		return null;

	}

}
