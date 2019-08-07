package org.emoflon.neo.neo4j.adapter.patterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.emoflon.neo.emsl.eMSL.ConstraintReference;
import org.emoflon.neo.emsl.eMSL.NegativeConstraint;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.eMSL.PositiveConstraint;
import org.emoflon.neo.neo4j.adapter.CypherPatternBuilder;
import org.emoflon.neo.neo4j.adapter.NeoCondition;
import org.emoflon.neo.neo4j.adapter.NeoConstraint;
import org.emoflon.neo.neo4j.adapter.NeoCoreBuilder;
import org.emoflon.neo.neo4j.adapter.NeoMask;
import org.emoflon.neo.neo4j.adapter.NeoMatch;
import org.emoflon.neo.neo4j.adapter.NeoNegativeConstraint;
import org.emoflon.neo.neo4j.adapter.NeoPositiveConstraint;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;

public class NeoPatternQueryAndMatch extends NeoPattern {
	private NeoCoreBuilder builder;

	public NeoPatternQueryAndMatch(Pattern p, NeoCoreBuilder builder, NeoMask mask) {
		this(p, builder);
		this.mask = Optional.of(mask);
	}

	/**
	 * 
	 * @param p       the given pattern from the model
	 * @param builder for creating and running Cypher queries
	 */
	public NeoPatternQueryAndMatch(Pattern p, NeoCoreBuilder builder) {
		super(p);
		this.builder = builder;

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
	 * Checks if a specify match is still valid, is still correctly in the database
	 * 
	 * @param m NeoMatch the match that should be checked
	 * @return true if the match is still valid or false if not
	 */
	@Override
	public boolean isStillValid(NeoMatch m) {
		// Run a normal pattern matching, if there is no condition
		if (p.getCondition() == null) {
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
			// If the condition is no direct Constraint (instead a Constraint Reference with
			// a Body, then create a new NeoCondition, with current data and follow the
			// structure from there for query execution
			if (p.getCondition() instanceof ConstraintReference) {
				var cond = new NeoCondition(new NeoConstraint(c, builder, helper), this, c.getName(), builder, helper);
				return cond.isStillValid(m);

			} else if (cond instanceof NeoPositiveConstraint) {

				var constraint = ((NeoPositiveConstraint) cond);

				// Condition is positive Constraint (ENFORCE xyz)
				logger.info("Check if match for " + p.getBody().getName() + " WHEN " + constraint.getName()
						+ " is still valid");

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
				logger.info("Check if match for " + p.getBody().getName() + " WHEN " + constraint.getName()
						+ " is still valid");

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
	public Record getData(NeoMatch m) {
		logger.info("Extract data from " + getName());
		var cypherQuery = CypherPatternBuilder.getDataQuery(nodes, m, injective);
		logger.debug(cypherQuery);
		StatementResult result = builder.executeQuery(cypherQuery);

		// Query is id-based and must be unique
		var results = result.list();
		if (results.size() != 1) {
			throw new IllegalStateException("Unable to extract data from match.\n"
					+ "There should be only one record but found: " + results.size());
		}
		return results.get(0);
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
		// Run a normal pattern matching, if there is no condition
		if (p.getCondition() == null) {
			logger.info("Searching matches for Pattern: " + getName());
			var cypherQuery = "";
			if (limit > 0)
				cypherQuery = CypherPatternBuilder.readQuery(nodes, injective, limit);
			else
				cypherQuery = CypherPatternBuilder.readQuery(nodes, injective);
			logger.debug(cypherQuery);

			var result = builder.executeQuery(cypherQuery);

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
				var cond = new NeoCondition(new NeoConstraint(c, builder, helper), this, c.getName(), builder, helper);
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
				var result = builder.executeQuery(cypherQuery);

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
				var result = builder.executeQuery(cypherQuery);

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
}
