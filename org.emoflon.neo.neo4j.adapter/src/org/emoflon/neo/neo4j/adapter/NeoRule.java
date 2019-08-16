package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.Constraint;
import org.emoflon.neo.emsl.eMSL.ConstraintReference;
import org.emoflon.neo.emsl.eMSL.NegativeConstraint;
import org.emoflon.neo.emsl.eMSL.PositiveConstraint;
import org.emoflon.neo.emsl.eMSL.Rule;
import org.emoflon.neo.emsl.refinement.EMSLFlattener;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.emsl.util.FlattenerException;
import org.emoflon.neo.engine.api.rules.IRule;

public class NeoRule implements IRule<NeoMatch, NeoCoMatch> {

	protected static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);

	protected List<NeoNode> nodes;
	protected List<NeoNode> nodesL;
	protected List<NeoNode> nodesR;
	protected List<NeoNode> nodesK;
	protected List<NeoRelation> relL;
	protected List<NeoRelation> relR;
	protected List<NeoRelation> relK;

	protected boolean injective;
	protected boolean spoSemantics; // if false: DPO; if true SPO semantics
	protected NeoHelper helper;
	
	protected Rule r;
	protected Constraint c;
	protected Object cond;

	protected IBuilder builder;
	protected NeoMask mask;

	public NeoRule(Rule r, NeoCoreBuilder builder, NeoMask mask) {
		nodes = new ArrayList<>();
		nodesL = new ArrayList<>();
		nodesR = new ArrayList<>();
		nodesK = new ArrayList<>();
		relL = new ArrayList<>();
		relR = new ArrayList<>();
		relK = new ArrayList<>();

		injective = true;
		spoSemantics = true;
		helper = new NeoHelper();
		this.mask = mask;
		this.builder = builder;
		try {
			this.r = (Rule) EMSLFlattener.flatten(r);
		} catch (FlattenerException e) {
			e.printStackTrace();
		}
		extractNodesAndRelations();
		
		if (r.getCondition() != null) {

			if (r.getCondition() instanceof ConstraintReference) {
				ConstraintReference ref = (ConstraintReference) r.getCondition();
				this.c = ref.getReference();

			} else if (r.getCondition() instanceof PositiveConstraint) {
				PositiveConstraint cons = (PositiveConstraint) r.getCondition();
				cond = (new NeoPositiveConstraint(cons.getPattern(), injective, builder, helper, mask));

			} else if (r.getCondition() instanceof NegativeConstraint) {
				NegativeConstraint cons = (NegativeConstraint) r.getCondition();
				cond = (new NeoNegativeConstraint(cons.getPattern(), injective, builder, helper, mask));

			} else {
				logger.info(r.getCondition().toString());
				throw new UnsupportedOperationException();
			}
		}
		
	}

	public NeoRule(Rule r, NeoCoreBuilder builder) {
		this(r, builder, new EmptyMask());
	}

	/**
	 * Creates and extracts all necessary information data from the flattened
	 * Pattern. Create new NeoNode for any AtomicPattern node and corresponding add
	 * Relations and Properties and save them to the node in an node list.
	 */
	private void extractNodesAndRelations() {

		for (var n : r.getNodeBlocks()) {

			var node = new NeoNode(n.getType().getName(), helper.newPatternNode(n.getName()));
			for (var p : n.getProperties()) {
				node.addProperty(p.getType().getName(), EMSLUtil.handleValue(p.getValue()));
			}
			
			extractPropertiesFromMask(node);

			for (var r : n.getRelations()) {

				var rel = new NeoRelation(node,
						helper.newPatternRelation(node.getVarName(), n.getRelations().indexOf(r),
								EMSLUtil.getAllTypes(r), r.getTarget().getName()),
						EMSLUtil.getAllTypes(r), //
						r.getLower(), r.getUpper(), //
						r.getProperties(), //
						r.getTarget().getType().getName(), //
						r.getTarget().getName());

				if (r.getAction() != null) {

					switch (r.getAction().getOp()) {
					case CREATE:
						relR.add(rel);
						relK.add(rel);
						logger.info("New ++ relation: (" + node.getVarName() + ")-[" + rel.getVarName() + ":"
								+ rel.getLower() + rel.getUpper() + "]->(" + rel.getToNodeVar() + ":"
								+ rel.getToNodeLabel() + ")");
						helper.removeMatchElement(rel.getVarName());
						break;
					case DELETE:
						relL.add(rel);
						node.addRelation(rel);
						logger.info("New -- relation: (" + node.getVarName() + ")-[" + rel.getVarName() + ":"
								+ rel.getLower() + rel.getUpper() + "]->(" + rel.getToNodeVar() + ":"
								+ rel.getToNodeLabel() + ")");
						break;
					default:
						throw new UnsupportedOperationException("Undefined Operator.");
					}

				} else {
					node.addRelation(rel);
					relK.add(rel);
					logger.info("New klebegraph relation: (" + node.getVarName() + ")-[" + rel.getVarName() + ":"
							+ rel.getLower() + rel.getUpper() + "]->(" + rel.getToNodeVar() + ":" + rel.getToNodeLabel()
							+ ")");
				}
			}
			
			if (n.getAction() != null) {

				switch (n.getAction().getOp()) {
				case CREATE:
					nodesR.add(node);
					logger.info("New ++ node: " + node.getVarName() + ":" + n.getType().getName());
					helper.removeMatchElement(node.getVarName());
					break;
				case DELETE:
					nodesL.add(node);
					nodes.add(node);
					logger.info("New -- node: " + node.getVarName() + ":" + n.getType().getName());
					break;
				default:
					throw new UnsupportedOperationException("Undefined Operator.");
				}

			} else {
				nodes.add(node);
				nodesK.add(node);
				logger.info("New klebegraph node: " + node.getVarName() + ":" + n.getType().getName());
			}
		}
	}

	protected void extractPropertiesFromMask(NeoNode node) {
		for (var propMask : mask.getMaskedAttributes().entrySet()) {
			var varName = mask.getVarName(propMask.getKey());
			if (node.getVarName().equals(varName)) {
				node.addProperty(//
						mask.getAttributeName(propMask.getKey()), //
						EMSLUtil.handleValue(propMask.getValue()));
			}

			for (var rel : node.getRelations()) {
				if (rel.getVarName().equals(varName)) {
					rel.addProperty(//
							mask.getAttributeName(propMask.getKey()), //
							EMSLUtil.handleValue(propMask.getValue()));
				}
			}
		}
	}

	@Override
	public String getName() {
		return r.getName();
	}

	@Override
	public void setMatchInjectively(Boolean injective) {
		this.injective = injective;
	}

	@Override
	public Collection<NeoMatch> determineMatches() {
		return determineMatches(0);
	}

	@Override
	public Collection<NeoMatch> determineMatches(int limit) {
		
		if (r.getCondition() == null) {
			
			logger.info("Searching matches for Pattern: " + getName());
			var cypherQuery = CypherPatternBuilder.readQuery(nodes, injective, limit, mask);
			logger.debug(cypherQuery);
	
			var result = builder.executeQuery(cypherQuery);
	
			var matches = new ArrayList<NeoMatch>();
			while (result.hasNext()) {
				var record = result.next();
				logger.info("MATCH FOUND");
				matches.add(new NeoMatch(this, record));
			}
	
			if (matches.isEmpty()) {
				logger.debug("NO MATCHES FOUND");
			}
			return matches;
			
		} else if (r.getCondition() instanceof ConstraintReference) {
			
			var cond = new NeoCondition(new NeoConstraint(c, builder, helper, mask), this, c.getName(), builder, helper);
			return cond.determineMatchesRule(limit);
			
		} else if (r.getCondition() instanceof PositiveConstraint) {
			
			var constraint = ((NeoPositiveConstraint) cond);

			// Condition is positive Constraint (ENFORCE xyz)
			logger.info("Searching matches for Pattern: " + constraint.getName() + " ENFORCE " + constraint.getName());

			// Create Query
			var cypherQuery = CypherPatternBuilder.constraintQuery(nodes, helper.getNodes(),
					constraint.getQueryString_MatchCondition(), constraint.getQueryString_WhereConditon(), injective, limit, mask);

			logger.debug(cypherQuery);

			// Execute query
			var result = builder.executeQuery(cypherQuery);

			// Analyze and return results
			var matches = new ArrayList<NeoMatch>();
			while (result.hasNext()) {
				var record = result.next();
				matches.add(new NeoMatch(this, record));
			}

			return matches;
			
		} else if (r.getCondition() instanceof NegativeConstraint) {
			var constraint = ((NeoNegativeConstraint) cond);
			
			// Condition is positive Constraint (ENFORCE xyz)
			logger.info("Searching matches for Pattern: " + constraint.getName() + " ENFORCE " + constraint.getName());

			// Create Query
			var cypherQuery = CypherPatternBuilder.constraintQuery(nodes, helper.getNodes(),
					constraint.getQueryString_MatchCondition(), constraint.getQueryString_WhereConditon(), injective, limit, mask);

			logger.debug(cypherQuery);

			// Execute query
			var result = builder.executeQuery(cypherQuery);

			// Analyze and return results
			var matches = new ArrayList<NeoMatch>();
			while (result.hasNext()) {
				var record = result.next();
				matches.add(new NeoMatch(this, record));
			}

			return matches;
			
		} else {
			throw new IllegalArgumentException("Unknown type of r:" + r);
		}
	}
	
	public boolean isStillApplicable(NeoMatch m) {
		
		if (r.getCondition() == null) {
			
			logger.info("Check if match for " + getName() + " is still valid");
			var cypherQuery = CypherPatternBuilder.isStillValidQuery(nodes, m, injective);
			logger.debug(cypherQuery);
			var result = builder.executeQuery(cypherQuery);
	
			// Query is id-based and must be unique
			var results = result.list();
			if (results.size() > 1) {
				throw new IllegalStateException("There should be at most one record found not " + results.size());
			}
	
			return results.size() == 1;
			
		} else {
			
			//throw new UnsupportedOperationException();
			
			// If the condition is no direct Constraint (instead a Constraint Reference with
			// a Body, then create a new NeoCondition, with current data and follow the
			// structure from there for query execution
			if (r.getCondition() instanceof ConstraintReference) {
				var cond = new NeoCondition(new NeoConstraint(c, builder, helper, mask), this, c.getName(), builder, helper);
				return cond.isStillValid(m);

			} else if (cond instanceof NeoPositiveConstraint) {

				var constraint = ((NeoPositiveConstraint) cond);

				// Condition is positive Constraint (ENFORCE xyz)
				logger.info("Check if match for " + r.getName() + " WHEN " + constraint.getName() + " is still valid");

				// Create Query
				var cypherQuery = CypherPatternBuilder.constraintQuery_isStillValid(nodes, helper.getNodes(),
						constraint.getQueryString_MatchCondition(), constraint.getQueryString_WhereConditon(),
						injective, m);

				logger.debug(cypherQuery);

				// Execute query
				var result = builder.executeQuery(cypherQuery);
				return result.hasNext();

			} else if (cond instanceof NeoNegativeConstraint) {

				var constraint = ((NeoNegativeConstraint) cond);

				// Condition is positive Constraint (ENFORCE xyz)
				logger.info("Check if match for " + r.getName() + " WHEN " + constraint.getName() + " is still valid");

				// Create Query
				var cypherQuery = CypherPatternBuilder.constraintQuery_isStillValid(nodes, helper.getNodes(),
						constraint.getQueryString_MatchCondition(), constraint.getQueryString_WhereConditon(),
						injective, m);

				logger.debug(cypherQuery);

				// Execute query
				var result = builder.executeQuery(cypherQuery);
				return result.hasNext();

			} else {
				// Note: If/Then conditions are currently not supported
				throw new UnsupportedOperationException();
			}
		}
		
	}

	@Override
	public Optional<NeoCoMatch> apply(NeoMatch match) {

		logger.info("Execute Rule " + getName());
		var cypherQuery = CypherPatternBuilder.ruleExecutionQuery(nodes, match, spoSemantics, nodesL, nodesR, nodesK, relL, relR, relK);
		logger.debug(cypherQuery);
		var result = builder.executeQuery(cypherQuery);
		
		if (result.hasNext()) {
			var record = result.next();
			return Optional.of(new NeoCoMatch(this, record));
		} else {
			return Optional.empty();
		}

	}

	// RuleApplicationSemantics.DoublePushOut
	public void setSPOSemantics(boolean spoSemantics) {
		this.spoSemantics = spoSemantics;
	}
	
	public Collection<NeoNode> getNodes() {
		return nodes;
	}
	
	public boolean isNegated() {
		if (r.getCondition() != null)
			return ((ConstraintReference) (r.getCondition())).isNegated();
		else
			return false;
	}

}
