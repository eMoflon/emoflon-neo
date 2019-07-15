package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.Constraint;
import org.emoflon.neo.emsl.eMSL.ConstraintReference;
import org.emoflon.neo.emsl.eMSL.NegativeConstraint;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.eMSL.PositiveConstraint;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.engine.api.rules.IPattern;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;

/**
 * Class for representing an in EMSL defined pattern for creating pattern
 * matching or condtion queries
 * 
 * @author Jannik Hinz
 *
 */
public class NeoPattern implements IPattern<NeoMatch> {
	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);

	private Optional<NeoCoreBuilder> builder;
	private NeoHelper helper;

	private Pattern p;
	private Constraint c;
	private Object cond;
	private boolean injective;

	private List<NeoNode> nodes;

	public NeoPattern(Pattern p, NeoCoreBuilder builder, NeoMask mask) {
		// TODO[Jannik] Use mask to fix parameters for the query
		this(p, builder);
	}
	
	public NeoPattern(Pattern p, NeoCoreBuilder builder) {
		this(p, Optional.of(builder));
	}

	/**
	 * 
	 * @param p       the given pattern from the model
	 * @param builder for creating and running Cypher queries
	 */
	public NeoPattern(Pattern p, Optional<NeoCoreBuilder> builder) {
		nodes = new ArrayList<>();
		injective = true;
		this.builder = builder;
		this.helper = new NeoHelper();

		// execute the Pattern flatterer. Needed if the pattern use refinements or other
		// functions. Returns the complete flattened Pattern.
		this.p = helper.getFlattenedPattern(p);

		// get all nodes, relations and properties from the pattern
		extractNodesAndRelations();

		// check if the current pattern has Condition, which must be checked also
		// (possible constraints can be Positive, Negative or nested in OR/AND Bodies)
		if (p.getCondition() != null) {

			if (p.getCondition() instanceof ConstraintReference) {
				ConstraintReference ref = (ConstraintReference) p.getCondition();
				this.c = ref.getReference();

			} else if (p.getCondition() instanceof PositiveConstraint) {
				PositiveConstraint cons = (PositiveConstraint) p.getCondition();
				cond = (new NeoPositiveConstraint(cons.getPattern(), injective, builder, helper));

			} else if (p.getCondition() instanceof NegativeConstraint) {
				NegativeConstraint cons = (NegativeConstraint) p.getCondition();
				cond = (new NeoNegativeConstraint(cons.getPattern(), injective, builder, helper));

			} else {
				logger.info(p.getCondition().toString());
				throw new UnsupportedOperationException();
			}
		}
	}

	/**
	 * Creates and extracts all necessary information data from the flattend
	 * Pattern. Create new NeoNode for any AtomicPattern node and corresponding add
	 * Relations and Properties and save them to the node in an node list.
	 */
	private void extractNodesAndRelations() {

		for (var n : p.getBody().getNodeBlocks()) {

			var node = new NeoNode(n.getType().getName(), helper.newPatternNode(n.getName()));

			n.getProperties().forEach(p -> node.addProperty(//
					p.getType().getName(), //
					EMSLUtil.handleValue(p.getValue())));

			// TODO[Jannik] Think of how to handle optional edges with multiple types
			n.getRelations()
					.forEach(r -> node.addRelation(
							helper.newPatternRelation(node.getVarName(), n.getRelations().indexOf(r),
									EMSLUtil.getOnlyType(r).getName(), r.getTarget().getName()),
							EMSLUtil.getOnlyType(r).getName(), //
							r.getProperties(), //
							r.getTarget().getType().getName(), //
							r.getTarget().getName()));

			nodes.add(node);
		}
	}

	/**
	 * Return the name of the given Pattern
	 * 
	 * @return name of the pattern
	 */
	@Override
	public String getName() {
		return p.getBody().getName();
	}

	/**
	 * Return a NeoNode list of all nodes in the pattern
	 * 
	 * @return NeoNode list of nodes in the pattern
	 */
	public List<NeoNode> getNodes() {
		return nodes;
	}

	/**
	 * Get the data and nodes from the pattern (and conditions) and runs the query
	 * in the database, analyze the results and return the matches
	 * 
	 * @return Collection<IMatch> return a list of all Matches of the pattern with
	 *         condition matching
	 */
	@Override
	public Collection<NeoMatch> determineMatches() {
		return determineMatches(0);
	}

	/**
	 * Checks if a specifiy match is still valid, is still correctly in the database
	 * 
	 * @param m NeoMatch the match that should be checked
	 * @return true if the match is still valid or false if not
	 */
	public boolean isStillValid(NeoMatch m) {
		
		var bld = builder.orElseThrow();
		
		logger.info("Check if match for " + getName() + " is still valid");
		var cypherQuery = CypherPatternBuilder.isStillValidQuery(nodes, m, injective);
		logger.debug(cypherQuery);
		var result = bld.executeQuery(cypherQuery);

		// Query is id-based and must be unique
		var results = result.list();
		if (results.size() > 1) {
			throw new IllegalStateException("There should be at most one record found not " + results.size());
		}

		return results.size() == 1;
	}

	/**
	 * Return is the given pattern an base on the constraint reference is negated
	 * 
	 * @return boolean if or not the given result of constraint reference must be
	 *         negated
	 */
	protected boolean isNegated() {
		if (p.getCondition() != null)
			return ((ConstraintReference) (p.getCondition())).isNegated();
		else
			return false;
	}

	public Record getData(NeoMatch m) {
		
		var bld = builder.orElseThrow();
		
		logger.info("Extract data from " + getName());
		var cypherQuery = CypherPatternBuilder.getDataQuery(nodes, m, injective);
		logger.debug(cypherQuery);
		StatementResult result = bld.executeQuery(cypherQuery);

		// Query is id-based and must be unique
		var results = result.list();
		if (results.size() != 1) {
			throw new IllegalStateException("Unable to extract data from match.\n"
					+ "There should be only one record but found: " + results.size());
		}
		return results.get(0);
	}
	
	public String getQuery() {
		
		if (p.getCondition() == null) {
			return CypherPatternBuilder.readQuery_copyPaste(nodes, injective);
		} else {

			if (p.getCondition() instanceof ConstraintReference) {
				var cond = new NeoCondition(new NeoConstraint(c, Optional.empty(), helper), this, c.getName(),
						Optional.empty(), helper);
				return cond.getQuery();

			} else if (cond instanceof NeoPositiveConstraint) {

				var constraint = ((NeoPositiveConstraint) cond);
				return CypherPatternBuilder.constraintQuery_copyPaste(nodes, helper.getNodes(),
						constraint.getQueryString_MatchCondition(), constraint.getQueryString_WhereConditon(),
						injective, 0);

			} else if (cond instanceof NeoNegativeConstraint) {

				var constraint = ((NeoNegativeConstraint) cond);
				return CypherPatternBuilder.constraintQuery_copyPaste(nodes, helper.getNodes(),
						constraint.getQueryString_MatchCondition(), constraint.getQueryString_WhereConditon(),
						injective, 0);
			} else {
				// Note: If/Then conditions are currently not supported
				throw new UnsupportedOperationException();
			}
		}
	}

	/**
	 * Set is the pattern should be injective or not
	 * 
	 * @param injective is the pattern should be injective matched
	 */
	@Override
	public void setMatchInjectively(Boolean injective) {
		this.injective = injective;
	}

	/**
	 * Get the data and nodes from the pattern (and conditions) and runs the query
	 * in the database, analyze the results and return the matches
	 * 
	 * @param limit number of matches, that should be returned - 0 if infinite
	 * @return Collection<IMatch> return a list of all Matches of the pattern with
	 *         condition matching
	 */
	@Override
	public Collection<NeoMatch> determineMatches(int limit) {
		
		var bld = builder.orElseThrow();
		
		// Run a normal pattern matching, if there is no condition
		if (p.getCondition() == null) {
			logger.info("Searching matches for Pattern: " + getName());
			var cypherQuery = "";
			if (limit > 0)
				cypherQuery = CypherPatternBuilder.readQuery(nodes, injective, limit);
			else
				cypherQuery = CypherPatternBuilder.readQuery(nodes, injective);
			logger.debug(cypherQuery);

			var result = bld.executeQuery(cypherQuery);

			var matches = new ArrayList<NeoMatch>();
			while (result.hasNext()) {
				var record = result.next();
				matches.add(new NeoMatch(this, record));
			}

			if (matches.isEmpty()) {
				logger.debug("NO MATCHES FOUND");
			}
			return matches;
		}
		// Create and Run a Condition Matching, if the pattern has a condition
		else {

			// If the condition is no direct Constraint (instead a Constraint Reference with
			// a Body, then create a new NeoCondition, with current data and follow the
			// structure from there for query execution
			if (p.getCondition() instanceof ConstraintReference) {
				var cond = new NeoCondition(new NeoConstraint(c, bld, helper), this, c.getName(),
						bld, helper);
				if (limit > 0)
					return cond.determineMatches(limit);
				else
					return cond.determineMatches();

			} else if (cond instanceof NeoPositiveConstraint) {

				var constraint = ((NeoPositiveConstraint) cond);

				// Condition is positive Constraint (ENFORCE xyz)
				logger.info(
						"Searching matches for Pattern: " + p.getBody().getName() + " ENFORCE " + constraint.getName());

				// Create Query
				var cypherQuery = CypherPatternBuilder.constraintQuery(nodes, helper.getNodes(),
						constraint.getQueryString_MatchCondition(), constraint.getQueryString_WhereConditon(),
						injective, limit);

				logger.debug(cypherQuery);

				// Execute query
				var result = bld.executeQuery(cypherQuery);

				// Analyze and return results
				var matches = new ArrayList<NeoMatch>();
				while (result.hasNext()) {
					var record = result.next();
					matches.add(new NeoMatch(this, record));
				}

				return matches;

			} else if (cond instanceof NeoNegativeConstraint) {

				var constraint = ((NeoNegativeConstraint) cond);

				// Condition is negative Constraint (FORBID xyz)
				logger.info(
						"Searching matches for Pattern: " + p.getBody().getName() + " FORBID " + constraint.getName());

				// create query
				var cypherQuery = CypherPatternBuilder.constraintQuery(nodes, helper.getNodes(),
						constraint.getQueryString_MatchCondition(), constraint.getQueryString_WhereConditon(),
						injective, limit);
				logger.debug(cypherQuery);

				// execute query
				var result = bld.executeQuery(cypherQuery);

				// analyze and return results
				var matches = new ArrayList<NeoMatch>();
				while (result.hasNext()) {
					var record = result.next();
					matches.add(new NeoMatch(this, record));
				}

				return matches;

			} else {
				// Note: If/Then conditions are currently not supported
				throw new UnsupportedOperationException();
			}
		}
	}

	/**
	 * Get the injectivity information of a pattern
	 * 
	 * @return boolean true if the given pattern requires injective pattern matching
	 */
	public boolean isInjective() {
		return injective;
	}

	public Pattern getPattern() {
		return p;
	}

	/**
	 * Runs the pattern matching and counts size of matches
	 * 
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
