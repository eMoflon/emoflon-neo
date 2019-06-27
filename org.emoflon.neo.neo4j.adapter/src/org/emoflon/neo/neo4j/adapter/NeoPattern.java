package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.EMSLFlattener;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.emsl.eMSL.Constraint;
import org.emoflon.neo.emsl.eMSL.ConstraintReference;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement;
import org.emoflon.neo.emsl.util.FlattenerException;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.api.rules.IPattern;
import org.neo4j.driver.v1.StatementResult;

public class NeoPattern implements IPattern {
	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);

	private NeoCoreBuilder builder;
	private Pattern p;
	private Constraint c;
	private boolean injective;

	private List<NeoNode> nodes;

	public NeoPattern(Pattern p, NeoCoreBuilder builder) {
		nodes = new ArrayList<>();
		injective = true;
		this.builder = builder;

		try {
			this.p = new EMSLFlattener().flattenCopyOfPattern(p, new ArrayList<String>());
		} catch (FlattenerException e) {
			logger.error("EMSL Flattener was unable to process the pattern.");
			e.printStackTrace();
		}

		extractNodesAndRelations();
		
		if(p.getCondition() != null) {
			this.c = (Constraint) p.getCondition().eCrossReferences().get(0);
		}		

	}

	public NeoPattern(AtomicPattern ap, NeoCoreBuilder builder) {
		nodes = new ArrayList<>();
		injective = true;
		this.builder = builder;
		extractNodesAndRelations(ap);
	}

	private void extractNodesAndRelations() {
		for (var n : p.getBody().getNodeBlocks()) {
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

	private void extractNodesAndRelations(AtomicPattern ap) {

		for (ModelNodeBlock n : ap.getNodeBlocks()) {

			var node = new NeoNode(n.getType().getName(), n.getName());

			for (ModelPropertyStatement p : n.getProperties()) {
				node.addProperty(p.getType().getName(), NeoUtil.handleValue(p.getValue()));
			}
			for (ModelRelationStatement r : n.getRelations()) {
				node.addRelation(new NeoRelation(node, //
						n.getRelations().indexOf(r), //
						r.getType().getName(), //
						r.getProperties(), //
						r.getTarget().getType().getName(), //
						r.getTarget().getName()));
			}
			nodes.add(node);
		}
	}

	@Override
	public String getName() {
		return p.getBody().getName();
	}

	public List<NeoNode> getNodes() {
		return nodes;
	}

	@Override
	public Collection<IMatch> determineMatches() {
		
		if(c == null) {
			logger.info("Searching matches for Pattern: " + getName());
			var cypherQuery = CypherPatternBuilder.readQuery(nodes, injective);
			logger.debug(cypherQuery);
			
			var result = builder.executeQuery(cypherQuery);
			
			var matches = new ArrayList<IMatch>();
			while (result.hasNext()) {
				var record = result.next();
				matches.add(new NeoMatch(this, record));	
			}
			
			if (matches.isEmpty()) {
				logger.debug("NO MATCHES FOUND");
				return null;
			} else {
				return matches;
			}
		} else {
			// check condition
			var cond = new NeoCondition(new NeoConstraint(c, builder), this, c.getName(), builder);		
			return cond.determineMatches();
			
		} 

	}

	public boolean isStillValid(NeoMatch m) {
		logger.info("Check if match for " + getName() + " is still valid");
		var cypherQuery = CypherPatternBuilder.isStillValidQuery(nodes, m, injective);
		logger.debug(cypherQuery);
		StatementResult result = builder.executeQuery(cypherQuery);
		return result.hasNext();
	}

	@Override
	public void setMatchInjectively(Boolean injective) {
		this.injective = injective;
	}
}
