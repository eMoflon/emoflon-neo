package org.emoflon.neo.neo4j.adapter.patterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.engine.api.patterns.IPattern;
import org.emoflon.neo.neo4j.adapter.common.NeoNode;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.templates.CypherPatternBuilder;
import org.emoflon.neo.neo4j.adapter.util.NeoQueryData;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.exceptions.DatabaseException;

import com.google.common.collect.Streams;

/**
 * Class for representing an in EMSL defined pattern for creating pattern
 * matching or condition queries
 * 
 * @author Jannik Hinz
 *
 */
public abstract class NeoPattern implements IPattern<NeoMatch> {
	private static final Logger logger = Logger.getLogger(NeoPattern.class);

	protected List<NeoNode> nodes;
	protected boolean injective;
	protected NeoQueryData queryData;
	protected IBuilder builder;
	protected NeoMask mask;
	protected String name;
	protected boolean isNegated;

	protected NeoPattern(List<ModelNodeBlock> nodeBlocks, String name, IBuilder builder, NeoMask mask,
			NeoQueryData queryData) {
		nodes = new ArrayList<>();
		injective = true;
		this.queryData = queryData;

		this.builder = builder;
		this.mask = mask;
		this.name = name;

		// get all nodes, relations and properties from the pattern
		nodes = queryData.extractPatternNodesAndRelations(nodeBlocks);
		nodes.forEach(this::extractPropertiesFromMask);
	}

	protected void extractPropertiesFromMask(NeoNode node) {
		for (var propMask : mask.getMaskedAttributes().entrySet()) {
			var varName = mask.getVarName(propMask.getKey());
			if (node.getVarName().equals(varName)) {
				node.addProperty(//
						mask.getAttributeName(propMask.getKey()), //
						EMSLUtil.returnValueAsString(propMask.getValue()));
			}

			for (var rel : node.getRelations()) {
				if (rel.getVarName().equals(varName)) {
					rel.addProperty(//
							mask.getAttributeName(propMask.getKey()), //
							EMSLUtil.returnValueAsString(propMask.getValue()));
				}
			}
		}
	}
	
	public void addExtraNodes(List<NeoNode> nodes) {
		this.nodes.addAll(nodes);
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
		return name;
	}

	/**
	 * Return a NeoNode list of all nodes in the pattern
	 * 
	 * @return NeoNode list of nodes in the pattern
	 */
	public List<NeoNode> getNodes() {
		return List.copyOf(nodes);
	}


	/**
	 * Runs the pattern matching and counts size of matches
	 * 
	 * @return Number of matches
	 */
	@Override
	public int countMatches() {
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
	public abstract Map<String, Boolean> isStillValid(Collection<NeoMatch> neoMatch);

	public abstract String getQuery();

	protected String getQuery(String matchCond, String whereCond) {
		return CypherPatternBuilder.constraintQuery_copyPaste(//
				nodes, //
				queryData.getAllElements(), //
				matchCond, //
				whereCond, //
				queryData.getAttributeExpressions(),
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
	
	public Collection<Record> getData(Collection<? extends NeoMatch> m) {
		logger.debug("Extract data from " + getName());
		
		var cypherQuery = CypherPatternBuilder.getDataQueryCollection(nodes, queryData.getAttributeExpressions(), injective);

		var list = new ArrayList<Map<String,Object>>();
		m.forEach(match -> list.add(match.getParameters()));
		
		var map = new HashMap<String,Object>();
		map.put("matches",list);
		
		logger.debug(map.toString() + "\n" + cypherQuery);
		
		StatementResult result = builder.executeQueryWithParameters(cypherQuery, map);

		if(result == null) {
			throw new DatabaseException("400", "Execution Error: See console log for more details.");
		} else {
			var results = result.list();
			return results;
		}
	}
	
	
	@Override
	public Stream<String> getPatternElts() {
		var nodeNames = nodes.stream().map(n -> n.getVarName());
		var edgeNames = nodes.stream().flatMap(n -> n.getRelations().stream()).map(r -> r.getVarName());
		
		return Streams.concat(nodeNames, edgeNames);
	}
}
