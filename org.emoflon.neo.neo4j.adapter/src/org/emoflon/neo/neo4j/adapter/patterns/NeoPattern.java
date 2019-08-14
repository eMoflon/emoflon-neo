package org.emoflon.neo.neo4j.adapter.patterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.ConstraintReference;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.engine.api.rules.IPattern;
import org.emoflon.neo.neo4j.adapter.CypherPatternBuilder;
import org.emoflon.neo.neo4j.adapter.IBuilder;
import org.emoflon.neo.neo4j.adapter.NeoCoreBuilder;
import org.emoflon.neo.neo4j.adapter.NeoHelper;
import org.emoflon.neo.neo4j.adapter.NeoMask;
import org.emoflon.neo.neo4j.adapter.NeoMatch;
import org.emoflon.neo.neo4j.adapter.NeoNode;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;

/**
 * Class for representing an in EMSL defined pattern for creating pattern
 * matching or condtion queries
 * 
 * @author Jannik Hinz
 *
 */
public abstract class NeoPattern implements IPattern<NeoMatch> {
	protected static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);

	protected List<NeoNode> nodes;
	protected boolean injective;
	protected NeoHelper helper;
	protected Pattern p;

	protected IBuilder builder;
	protected NeoMask mask;

	protected NeoPattern(Pattern p, IBuilder builder, NeoMask mask) {
		nodes = new ArrayList<>();
		injective = true;
		helper = new NeoHelper();

		this.builder = builder;
		this.mask = mask;

		// execute the Pattern flatterer. Needed if the pattern use refinements or other
		// functions. Returns the complete flattened Pattern.
		this.p = helper.getFlattenedPattern(p);

		// get all nodes, relations and properties from the pattern
		extractNodesAndRelations();
	}

	/**
	 * Creates and extracts all necessary information data from the flattened
	 * Pattern. Create new NeoNode for any AtomicPattern node and corresponding add
	 * Relations and Properties and save them to the node in an node list.
	 */
	private void extractNodesAndRelations() {
		for (var n : p.getBody().getNodeBlocks()) {
			var node = new NeoNode(n.getType().getName(), helper.newPatternNode(n.getName()));

			n.getProperties().forEach(p -> node.addProperty(//
					p.getType().getName(), //
					EMSLUtil.handleValue(p.getValue())));

			extractPropertiesFromMask(node);

			n.getRelations()
					.forEach(r -> node.addRelation(
							helper.newPatternRelation(node.getVarName(), n.getRelations().indexOf(r),
									EMSLUtil.getAllTypes(r), r.getTarget().getName()),
							EMSLUtil.getAllTypes(r), //
							r.getLower(), r.getUpper(), //
							r.getProperties(), //
							r.getTarget().getType().getName(), //
							r.getTarget().getName()));

			nodes.add(node);
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

	/**
	 * Checks if a specify match is still valid, is still correctly in the database
	 * 
	 * @param m NeoMatch the match that should be checked
	 * @return true if the match is still valid or false if not
	 */
	public abstract boolean isStillValid(NeoMatch neoMatch);

	/**
	 * Return is the given pattern an base on the constraint reference is negated
	 * 
	 * @return boolean if or not the given result of constraint reference must be
	 *         negated
	 */
	public boolean isNegated() {
		if (p.getCondition() != null)
			return ((ConstraintReference) (p.getCondition())).isNegated();
		else
			return false;
	}

	public abstract String getQuery();

	protected String getQuery(String matchCond, String whereCond) {
		return CypherPatternBuilder.constraintQuery_copyPaste(//
				nodes, //
				helper.getNodes(), //
				matchCond, //
				whereCond, //
				injective, //
				0);
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
}
