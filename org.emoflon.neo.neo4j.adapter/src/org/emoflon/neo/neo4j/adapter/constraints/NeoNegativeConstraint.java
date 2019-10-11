package org.emoflon.neo.neo4j.adapter.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.engine.api.constraints.INegativeConstraint;
import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.neo4j.adapter.common.NeoNode;
import org.emoflon.neo.neo4j.adapter.common.NeoRelation;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMask;
import org.emoflon.neo.neo4j.adapter.patterns.NeoPattern;
import org.emoflon.neo.neo4j.adapter.patterns.NeoPatternFactory;
import org.emoflon.neo.neo4j.adapter.templates.CypherPatternBuilder;
import org.emoflon.neo.neo4j.adapter.util.NeoQueryData;
import org.emoflon.neo.neo4j.adapter.util.NeoUtil;

/**
 * Class representing an FORBID constraint, storing all relevant data, creates
 * and runs the query for checking the constraint
 * 
 * @author Jannik Hinz
 *
 */
public class NeoNegativeConstraint extends NeoConstraint implements INegativeConstraint {
	private String name;
	private List<NeoNode> nodes;
	private final int uuid;
	private NeoPattern negativePattern;

	/**
	 * 
	 * @param ap        AtomicPattern of the FORBID constraint
	 * @param injective boolean if the pattern should be matches injective or not
	 * @param builder   for creating and running Cypher queries
	 * @param queryData    for creating nodes and
	 */
	public NeoNegativeConstraint(AtomicPattern ap, boolean injective, IBuilder builder, NeoQueryData queryData,
			NeoMask mask) {
		super(builder, queryData, mask, injective);

		this.uuid = queryData.incrementCounterForConstraintsInQuery();
		this.name = ap.getName();
		
		this.negativePattern = NeoPatternFactory.createNeoPattern(ap, builder, mask);
		
		var flatPattern = NeoUtil.getFlattenedPattern(ap);

		// Extracts all necessary information data from the Atomic Pattern
		this.nodes = new ArrayList<>(this.queryData.extractConstraintNodesAndRelations(flatPattern.getNodeBlocks()));
	
		this.returnAsCondition = createReturnStatement(getNodes(), getQueryString_MatchCondition(), getQueryString_WhereCondition());
		this.returnAsConstraint = createReturnStatement(getNodes(), getQueryString_MatchConstraint(), getQueryString_WhereConstraint());
	}

	/**
	 * Return the name of the constraint
	 * 
	 * @return name of the constraint
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Returns a collection of the Nodes from the constraint
	 * 
	 * @return NeoNode collection of the Nodes from the constraint
	 */
	public Collection<NeoNode> getNodes() {
		return nodes;
	}

	/**
	 * Return the corresponding OPTIONAL MATCH query with injectivity block and WITH
	 * clause (for use in constraints)
	 * 
	 * @return OPTIONAL MATCH xyz WHERE injective WITH nodes (query part for this
	 *         constraint)
	 */
	public String getQueryString_MatchConstraint() {
		return CypherPatternBuilder.constraint_matchQuery(nodes, queryData.getAttributeExpressionsOptional(), injective, uuid, mask);
	}

	/**
	 * Return the corresponding OPTIONAL MATCH query with injectivity block and WITH
	 * clause (for use in conditions)
	 * 
	 * @return OPTIONAL MATCH xyz WHERE injective WITH nodes (query part for this
	 *         constraint)
	 */
	public String getQueryString_MatchCondition() {
		return CypherPatternBuilder.condition_matchQuery(nodes, queryData.getAttributeExpressionsOptional(), injective, mask, queryData.getEqualElements(), queryData.getAllNodesRequireInjectivityChecksCondition());
	}

	/**
	 * Return the corresponding WHERE query (for use in constraints)
	 * 
	 * @return WHERE count(xyz) = 0 (query part for this constraint)
	 */
	public String getQueryString_WhereConstraint() {
		return CypherPatternBuilder.whereNegativeConstraintQuery(uuid);
	}

	/**
	 * Return the corresponding WHERE query (for use in conditions)
	 * 
	 * @return WHERE xy IS NULL OR yz IS NULL (query part for this constraint)
	 */
	public String getQueryString_WhereCondition() {
		var patternElements = queryData.getMatchElements();
		var optionalElements = new ArrayList<String>();
		
		for(NeoNode n: nodes) {
			if(!patternElements.contains(n.getVarName())) {
				optionalElements.add(n.getVarName());
			}
			
			for(NeoRelation r: n.getRelations()) {
				if(!patternElements.contains(r.getVarName())) {
					optionalElements.add(r.getVarName());
				}
			}
		}
		
		return CypherPatternBuilder.whereNegativeConditionQuery(optionalElements);
	}

	@Override
	public Collection<IMatch> getViolations() {
		return new ArrayList<IMatch>(negativePattern.determineMatches());
	}
}
