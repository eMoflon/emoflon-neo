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
import org.emoflon.neo.emsl.eMSL.NegativeConstraint;
import org.emoflon.neo.emsl.util.FlattenerException;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.api.rules.IPattern;
import org.neo4j.driver.v1.StatementResult;

/**
 * Class for representing an in EMSL defined pattern for creating pattern
 * matching or condtion queries
 * 
 * @author Jannik Hinz
 *
 */
public class NeoPattern implements IPattern {
	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);

	private NeoCoreBuilder builder;
	private NeoHelper helper;

	private Pattern p;
	private Constraint c;
	private Object cond;
	private boolean injective;

	private List<NeoNode> nodes;

	/**
	 * 
	 * @param p       the given pattern from the model
	 * @param builder for creating and running Cypher queries
	 */
	public NeoPattern(Pattern p, NeoCoreBuilder builder) {
		nodes = new ArrayList<>();
		injective = true;
		this.builder = builder;
		this.helper = new NeoHelper();

		// execute the Pattern flatterer. Needed if the pattern use refinements or other
		// functions. Returns the complete flattened Pattern.
		try {
			this.p = new EMSLFlattener().flattenCopyOfPattern(p, new ArrayList<String>());
		} catch (FlattenerException e) {
			logger.error("EMSL Flattener was unable to process the pattern.");
			e.printStackTrace();
		}

		// get all nodes, relations and properties from the pattern
		extractNodesAndRelations();

		// check if the current pattern has Condition, which must be checked also
		// (possible constraints can be Positive, Negative or nested in OR/AND Bodies)
		if (p.getCondition() != null) {

			if (p.getCondition() instanceof ConstraintReference) {
				this.c = (Constraint) p.getCondition().eCrossReferences().get(0);

			} else if (p.getCondition() instanceof PositiveConstraint) {
				cond = (NeoPositiveConstraint) (new NeoPositiveConstraint(
						(AtomicPattern) p.getCondition().eCrossReferences().get(0), injective, builder, helper));

			} else if (p.getCondition() instanceof NegativeConstraint) {
				cond = (NeoNegativeConstraint) (new NeoNegativeConstraint(
						(AtomicPattern) p.getCondition().eCrossReferences().get(0), injective, builder, helper));

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
					NeoUtil.handleValue(p.getValue())));

			n.getRelations().forEach(r -> node.addRelation(new NeoRelation(//
					node, //
					helper.newPatternRelation(node.getVarName(), n.getRelations().indexOf(r), r.getType().getName(),
							r.getTarget().getName()),
					r.getType().getName(), //
					r.getProperties(), //
					r.getTarget().getType().getName(), //
					r.getTarget().getName())));

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
	public Collection<IMatch> determineMatches() {

		// Run a normal pattern matching, if there is no condition
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

		}
		// Create and Run a Condition Matching, if the pattern has a condition
		else {

			// If the condition is no direct Constraint (instead a Constraint Reference with
			// a Body, then create a new NeoCondition, with current data and follow the
			// structure from there for query execution
			if (p.getCondition() instanceof ConstraintReference) {
				var cond = new NeoCondition(new NeoConstraint(c, builder, helper), this, c.getName(), builder, helper);
				return cond.determineMatches();

			} else if (cond instanceof NeoPositiveConstraint) {

				// Condition is positive Constraint (ENFORCE xyz)
				logger.info("Searching matches for Pattern: " + p.getBody().getName() + " ENFORCE "
						+ ((NeoPositiveConstraint) cond).getName());

				// Create Query
				var cypherQuery = CypherPatternBuilder.matchQuery(nodes);
				if (injective)
					cypherQuery += CypherPatternBuilder.injectivityBlock(nodes) + "\n";
				cypherQuery += CypherPatternBuilder.withQuery(nodes)
						+ ((NeoPositiveConstraint) cond).getQueryString_MatchCondition()
						+ CypherPatternBuilder.withConstraintQuery(helper.getNodes()) + "\nWHERE "
						+ ((NeoPositiveConstraint) cond).getQueryString_WhereConditon() + "\n"
						+ CypherPatternBuilder.withConstraintQuery(helper.getNodes()) + "\n"
						+ CypherPatternBuilder.returnQuery(nodes);

				logger.debug(cypherQuery);

				// Execute query
				var result = builder.executeQuery(cypherQuery);

				// Analyze and return results
				var matches = new ArrayList<IMatch>();
				while (result.hasNext()) {
					var record = result.next();
					matches.add(new NeoMatch(this, record));
				}

				return matches;

			} else if (cond instanceof NeoNegativeConstraint) {

				// Condition is negative Constraint (FORBID xyz)
				logger.info("Searching matches for Pattern: " + p.getBody().getName() + " FORBID "
						+ ((NeoNegativeConstraint) cond).getName());

				// create query
				var cypherQuery = CypherPatternBuilder.matchQuery(nodes);
				if (injective)
					cypherQuery += CypherPatternBuilder.injectivityBlock(nodes) + "\n";
				cypherQuery += CypherPatternBuilder.withQuery(nodes)
						+ ((NeoNegativeConstraint) cond).getQueryString_MatchCondition()
						+ CypherPatternBuilder.withConstraintQuery(helper.getNodes()) + "\nWHERE "
						+ ((NeoNegativeConstraint) cond).getQueryString_WhereConditon() + "\n"
						+ CypherPatternBuilder.withConstraintQuery(helper.getNodes()) + "\n"
						+ CypherPatternBuilder.returnQuery(nodes);

				logger.debug(cypherQuery);

				// execute query
				var result = builder.executeQuery(cypherQuery);

				// analyze and return results
				var matches = new ArrayList<IMatch>();
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
	 * Checks if a specifiy match is still valid, is still correctly in the database
	 * 
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
