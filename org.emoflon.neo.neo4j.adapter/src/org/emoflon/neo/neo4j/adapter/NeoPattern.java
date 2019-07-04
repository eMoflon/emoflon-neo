package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.EMSLFlattener;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.eMSL.PositiveConstraint;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.emsl.eMSL.Constraint;
import org.emoflon.neo.emsl.eMSL.ConstraintReference;
import org.emoflon.neo.emsl.eMSL.Implication;
import org.emoflon.neo.emsl.eMSL.NegativeConstraint;
import org.emoflon.neo.emsl.util.FlattenerException;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.api.rules.IPattern;
import org.neo4j.driver.v1.StatementResult;

public class NeoPattern implements IPattern {
	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);

	private NeoCoreBuilder builder;
	private NeoHelper helper;
	
	private Pattern p;
	private Constraint c;
	private Object cond;
	private boolean injective;

	private List<NeoNode> nodes;
	private Collection<String> nodesAndRefs;
	private Collection<NeoNode> nodesAndRefsN;

	public NeoPattern(Pattern p, NeoCoreBuilder builder) {
		nodes = new ArrayList<>();
		injective = true;
		this.builder = builder;
		this.helper = new NeoHelper();

		try {
			this.p = new EMSLFlattener().flattenCopyOfPattern(p, new ArrayList<String>());
		} catch (FlattenerException e) {
			logger.error("EMSL Flattener was unable to process the pattern.");
			e.printStackTrace();
		}

		extractNodesAndRelations();

		if (p.getCondition() != null) {

			if (p.getCondition() instanceof ConstraintReference) {
				this.c = (Constraint) p.getCondition().eCrossReferences().get(0);
				
			} else if (p.getCondition() instanceof PositiveConstraint) {
				cond = (NeoPositiveConstraint)(new NeoPositiveConstraint((AtomicPattern) p.getCondition().eCrossReferences().get(0), builder, injective, helper));

			} else if (p.getCondition() instanceof NegativeConstraint) {
				cond = (NeoNegativeConstraint)(new NeoNegativeConstraint((AtomicPattern) p.getCondition().eCrossReferences().get(0), builder, injective, helper));
				
			} else if (p.getCondition() instanceof Implication) {
				cond = (NeoImplication)(new NeoImplication((AtomicPattern) p.getCondition().eCrossReferences().get(0),
						(AtomicPattern) p.getCondition().eCrossReferences().get(1), builder, injective, helper));
			} else {
				logger.info(p.getCondition().toString());
				throw new UnsupportedOperationException();
			}
		}
	}

	private void extractNodesAndRelations() {
		
		for (var n : p.getBody().getNodeBlocks()) {
			
			var node = new NeoNode(n.getType().getName(), helper.newPatternNode(n.getName(), p));
			
			n.getProperties().forEach(p -> node.addProperty(//
					p.getType().getName(), //
					NeoUtil.handleValue(p.getValue())));
			
			n.getRelations().forEach(r -> node.addRelation(new NeoRelation(//
					node, //
					helper.newPatternRelation(node.getVarName(), n.getRelations().indexOf(r), r.getTarget().getType().getName(), p),
					r.getType().getName(), //
					r.getProperties(), //
					r.getTarget().getType().getName(), //
					r.getTarget().getName())));
			
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

		if (p.getCondition() == null) {
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
			}
			return matches;

		} else {
			if (p.getCondition() instanceof ConstraintReference) {
				// check condition
				var cond = new NeoCondition(new NeoConstraint(c, builder), this, c.getName(), builder);
				return cond.determineMatches();
				
			} else if (cond instanceof NeoPositiveConstraint) {
				
				logger.info("Searching matches for Pattern: " + p.getBody().getName() +
						" ENFORCE " + ((NeoPositiveConstraint) cond).getName());
				
				nodesAndRefs = new ArrayList<>();
				nodesAndRefsN = new ArrayList<>();
				removeDuplicates(nodes);			
				removeDuplicates(((NeoPositiveConstraint) cond).getNodes());
				
				var cypherQuery = CypherPatternBuilder.matchQuery(nodes);
				cypherQuery += ((NeoPositiveConstraint) cond).getQueryString_OptionalMatch();
				cypherQuery += CypherPatternBuilder.withConstraintQuery(nodesAndRefs);
				cypherQuery += "\nWHERE " + ((NeoPositiveConstraint) cond).getQueryString_Where();
				if(injective)
					cypherQuery += CypherPatternBuilder.injectivityBlockCond(nodesAndRefsN);
				cypherQuery += "\n" + CypherPatternBuilder.returnQuery(nodes);
				
				
				logger.debug(cypherQuery);

				var result = builder.executeQuery(cypherQuery);

				var matches = new ArrayList<IMatch>();
				while (result.hasNext()) {
					var record = result.next();
					matches.add(new NeoMatch(this, record));
				}
				
				return matches;
	
			} else if (cond instanceof NeoNegativeConstraint) {
				
				logger.info("Searching matches for Pattern: " + p.getBody().getName() +
						" FORBID " + ((NeoNegativeConstraint) cond).getName());
				
				nodesAndRefs = new ArrayList<>();
				nodesAndRefsN = new ArrayList<>();
				removeDuplicates(nodes);			
				removeDuplicates(((NeoNegativeConstraint) cond).getNodes());
				
				var cypherQuery = CypherPatternBuilder.matchQuery(nodes);
				cypherQuery += ((NeoNegativeConstraint) cond).getQueryString_OptionalMatch();
				cypherQuery += CypherPatternBuilder.withConstraintQuery(nodesAndRefs);
				cypherQuery += "\nWHERE " + ((NeoNegativeConstraint) cond).getQueryString_Where();
				if(injective)
					cypherQuery += CypherPatternBuilder.injectivityBlockCond(nodesAndRefsN);
				cypherQuery += "\n" + CypherPatternBuilder.returnQuery(nodes);
				
				
				logger.debug(cypherQuery);

				var result = builder.executeQuery(cypherQuery);

				var matches = new ArrayList<IMatch>();
				while (result.hasNext()) {
					var record = result.next();
					matches.add(new NeoMatch(this, record));
				}
				
				return matches;
				
			} else if (cond instanceof NeoImplication) {
				
				logger.info("Searching matches for Pattern: " + p.getBody().getName() + " " +
						((NeoImplication) cond).getName());
				
				nodesAndRefs = new ArrayList<>();
				nodesAndRefsN = new ArrayList<>();
				removeDuplicates(nodes);				
				removeDuplicates(((NeoImplication) cond).getIfNodes());
				removeDuplicates(((NeoImplication) cond).getThenNodes());
				
				var cypherQuery = CypherPatternBuilder.matchQuery(nodes);
				cypherQuery += ((NeoImplication) cond).getQueryString_OptionalMatch();
				cypherQuery += CypherPatternBuilder.withConstraintQuery(nodesAndRefs);
				cypherQuery += "\nWHERE " + ((NeoImplication) cond).getQueryString_Where();
				if(injective)
					cypherQuery += CypherPatternBuilder.injectivityBlockCond(nodesAndRefsN);
				cypherQuery += "\n" + CypherPatternBuilder.returnQuery(nodes);
				
				
				logger.debug(cypherQuery);

				var result = builder.executeQuery(cypherQuery);

				var matches = new ArrayList<IMatch>();
				while (result.hasNext()) {
					var record = result.next();
					matches.add(new NeoMatch(this, record));
				}
				
				return matches;
				
			} else {
				throw new UnsupportedOperationException();
			}
		}

	}

	/*
	 * Checks if a specifiy match is still valid, is still correctly in the database
	 * @param m NeoMatch the match that should be checked
	 * @return true if the match is still valid or false if not
	 */
	public boolean isStillValid(NeoMatch m) {
		logger.info("Check if match for " + getName() + " is still valid");
		var cypherQuery = CypherPatternBuilder.isStillValidQuery(nodes, m, injective);
		logger.debug(cypherQuery);
		StatementResult result = builder.executeQuery(cypherQuery);
		return result.hasNext();
	}
	
	/*
	 * Return is the given pattern an base on the constraint reference is negated
	 * @return boolean if or not the given result of constraint reference must be negated
	 */
	protected boolean isNegated() {
		if(p.getCondition() != null)
			return ((ConstraintReference)(p.getCondition())).isNegated();
		else
			return false;
	}
	
	/*
	 * Removes all duplicated nodes or relations from the node and relation list
	 * @param nodes nodes that should be added to the list (only if not present in the list)
	 */
	private void removeDuplicates(Collection<NeoNode> nodes) {
		for(NeoNode node : nodes) {
			
			if(!nodesAndRefs.contains(node.getVarName())) {
				nodesAndRefs.add(node.getVarName());
				nodesAndRefsN.add(node);
			}
			
			for(NeoRelation rel: node.getRelations()) {
				if(!nodesAndRefs.contains(rel.getVarName())) {
					nodesAndRefs.add(rel.getVarName());
				}
			}
		}
	}

	/*
	 * Set is the pattern should be injective or not
	 * @param injective is the pattern should be injective matched
	 */
	@Override
	public void setMatchInjectively(Boolean injective) {
		this.injective = injective;
	}
	
	/* Get the injectivity information of a pattern
	 * @return boolean true if the given pattern requires injective pattern matching
	 */
	public boolean isInjective() {
		return injective;
	}

	/*
	 * Runs the pattern matching and counts size of matches
	 * @return Number of matches
	 */
	@Override
	public Number countMatches() {
		var matches = determineMatches();
		if (matches != null)
			return matches.size();
		else
			return 0;
	}
}
