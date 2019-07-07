package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.engine.api.constraints.IIfElseConstraint;
import org.emoflon.neo.engine.api.rules.IMatch;

public class NeoImplication implements IIfElseConstraint {

	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	private NeoCoreBuilder builder;
	private NeoHelper helper;

	private AtomicPattern apIf;
	private AtomicPattern apThen;
	private String name;
	private List<NeoNode> nodesIf;
	private List<NeoNode> nodesThen;
	
	private boolean injective;
	private int uuid;

	public NeoImplication(AtomicPattern apIf, AtomicPattern apThen, NeoCoreBuilder builder, boolean injective, NeoHelper helper) {
		this.uuid = helper.addConstraint();
		this.builder = builder;
		this.helper = helper;
		this.apIf = apIf;
		this.apThen = apThen;
		this.name = "IF " + apIf.getName() + " THEN " + apThen.getName();
		nodesIf = new ArrayList<>();
		nodesThen = new ArrayList<>();
		this.injective = injective;
		extractNodesAndRelations();
	}

	public String getName() {
		return name;
	}

	public AtomicPattern getIfPattern() {
		return apIf;
	}

	public AtomicPattern getThenPattern() {
		return apThen;
	}
	
	private void extractNodesAndRelations() {
		
		for (var n : apIf.getNodeBlocks()) {
			
			var node = new NeoNode(n.getType().getName(), helper.newConstraintNode(n.getName(), apIf, uuid));
			
			n.getProperties().forEach(p -> node.addProperty(//
					p.getType().getName(), //
					NeoUtil.handleValue(p.getValue())));
			
			n.getRelations().forEach(r -> node.addRelation(new NeoRelation(//
					node, //
					helper.newConstraintReference(node.getVarName(), n.getRelations().indexOf(r), r.getTarget().getName(), apIf),
					r.getType().getName(), //
					r.getProperties(), //
					r.getTarget().getType().getName(), //
					helper.newConstraintNode(r.getTarget().getName(), apIf, uuid))));
			
			nodesIf.add(node);
		}
		for (var n : apThen.getNodeBlocks()) {
			
			//TODO: create methode for IfThen Constraints
			var node = new NeoNode(n.getType().getName(), helper.newConstraintNode(n.getName(), apIf, uuid));
			
			n.getProperties().forEach(p -> node.addProperty(//
					p.getType().getName(), //
					NeoUtil.handleValue(p.getValue())));
			
			n.getRelations().forEach(r -> node.addRelation(new NeoRelation(//
					node, //
					helper.newConstraintReference(node.getVarName(), n.getRelations().indexOf(r), r.getTarget().getName(), apIf),
					r.getType().getName(), //
					r.getProperties(), //
					r.getTarget().getType().getName(), //
					helper.newConstraintNode(r.getTarget().getName(), apIf, uuid))));
			nodesThen.add(node);
		}
	}
	
	public Collection<NeoNode> getIfNodes() {
		return nodesIf;
	}
	public Collection<NeoNode> getThenNodes() {
		return nodesThen;
	}
	public Collection<NeoNode> getNodes() {
		var list = nodesIf;
		nodesThen.forEach(elem -> list.add(elem));;
		return list;
	}
	
	public String getQueryString_Match() { 
		var query = "\nOPTIONAL " +  CypherPatternBuilder.matchQuery(nodesIf);
		if(injective) {
			query += CypherPatternBuilder.injectivityBlock(nodesIf);
		}
		query += "\nOPTIONAL " +  CypherPatternBuilder.matchQuery(nodesThen);
		if(injective) { 
			query += CypherPatternBuilder.injectivityBlock(nodesThen);
		}
		return query + "\n";
	}

	public String getQueryString_Where() {
		return "((" + CypherPatternBuilder.wherePositiveConstraintQuery(uuid) + ")" +
				" AND (" + CypherPatternBuilder.whereNegativeConstraintQuery(uuid) + "))"
	    ;
	}
	
	public String[] getQueryStringWhereCount() {
		
		var found = false;
		var uNode = "";
		
		for(NeoNode n: nodesThen) {
			if(uNode.equals("")) {
				for(NeoNode m: nodesIf) {
					if(n.getVarName().equals(m.getVarName())) {
						found = true;
					}
				}
				if(!found) {
					uNode = n.getVarName();
				}
			}
		}
		String[] ary = {"count(" + uNode + ") as c_" + uNode, "c_" + uNode + " = 0"};	
		return ary;
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
		logger.info("Check constraint: " + name);

		var cypherQuery = CypherPatternBuilder.readQuery(nodesIf, nodesThen, getNodes(), true);
		logger.debug(cypherQuery);

		var result = builder.executeQuery(cypherQuery);

		var matches = new ArrayList<IMatch>();
		while (result.hasNext()) {
			matches.add(new NeoMatch(null, result.next()));
		}

		if (!matches.isEmpty()) {
			logger.info("No invalid matches found. Constraint: " + name + " is NOT complied!");
			return matches;
		}
		
		logger.info("No invalid matches found. Constraint: " + name + " is complied!");
		return null;

	}

}
