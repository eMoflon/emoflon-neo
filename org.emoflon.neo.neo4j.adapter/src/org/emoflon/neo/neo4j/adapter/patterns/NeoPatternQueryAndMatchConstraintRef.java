package org.emoflon.neo.neo4j.adapter.patterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.emoflon.neo.emsl.eMSL.ConstraintReference;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.neo4j.adapter.constraints.NeoConstraint;
import org.emoflon.neo.neo4j.adapter.constraints.NeoConstraintFactory;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.templates.CypherPatternBuilder;
import org.emoflon.neo.neo4j.adapter.util.NeoQueryData;
import org.neo4j.driver.v1.exceptions.DatabaseException;

public class NeoPatternQueryAndMatchConstraintRef extends NeoPattern {
	protected NeoConstraint referencedConstraint;
	protected boolean isNegated;

	public NeoPatternQueryAndMatchConstraintRef(List<ModelNodeBlock> nodeBlocks, String name, ConstraintReference ref, IBuilder builder, NeoMask mask, NeoQueryData queryData) {
		super(nodeBlocks, name, builder, mask, queryData);
		isNegated = ref.isNegated();
		referencedConstraint = NeoConstraintFactory.createNeoConstraint(ref.getReference(), builder, queryData, mask);
	}

	@Override
	public Collection<NeoMatch> determineMatches(int limit) {
		logger.info("Searching matches for Pattern: " + getName() + " WHEN " + referencedConstraint.getName());

		// collecting the data
		var condData = referencedConstraint.getConditionData();

		// creating the query string
		var cypherQuery = CypherPatternBuilder.conditionQuery(getNodes(), condData.getOptionalMatchString(),
				condData.getWhereClause(), queryData.getAllElements(), isNegated, queryData.getAttributeExpressions(), injective, limit, mask);
		logger.debug(cypherQuery);

		// run the query
		var result = builder.executeQuery(cypherQuery);

		if(result == null) {
			throw new DatabaseException("400", "Execution Error: See console log for more details.");
		} else {
			var matches = new ArrayList<NeoMatch>();
			// analyze and return results
			while (result.hasNext()) {
				var record = result.next();
				matches.add(new NeoMatch(this, record));
			}
	
			return matches;
		}
	}
	
	/**
	 * Get the data and nodes from the (nested) conditions and runs the query in the
	 * database, analyze the results and return the matches
	 * 
	 * @param limit number of matches, that should be returned - 0 if infinite
	 * @return Collection<IMatch> return a list of all Matches of the pattern with
	 *         condition matching
	 */
	@Override
	public boolean isStillValid(NeoMatch m) {
		logger.info("Check if match for " + getName() + " WHEN " + referencedConstraint.getName() + " is still valid");

		// collecting the data
		var condData = referencedConstraint.getConditionData();

		// creating the query string
		var cypherQuery = CypherPatternBuilder.conditionQuery_isStillValid(getNodes(),
				condData.getOptionalMatchString(), condData.getWhereClause(), queryData.getAllElements(), queryData.getAttributeExpressions(), isNegated);
		logger.debug(m.getParameters().toString() + "\n" + cypherQuery);

		// run the query
		var result = builder.executeQueryWithParameters(cypherQuery, m.getParameters());

		if(result == null) {
			throw new DatabaseException("400", "Execution Error: See console log for more details.");
		} else {
			// analyze and return results
			return result.hasNext();
		}
	}
	
	@Override
	public Map<String,Boolean> isStillValid(Collection<NeoMatch> matches) {
		
		logger.info("Check if match for " + getName() + " WHEN " + referencedConstraint.getName() + " is still valid");

		// collecting the data
		var condData = referencedConstraint.getConditionData();

		var cypherQuery = CypherPatternBuilder.conditionQuery_isStillValid(getNodes(),
				condData.getOptionalMatchString(), condData.getWhereClause(), queryData.getAllElements(), queryData.getAttributeExpressions(), isNegated);
		
		var list = new ArrayList<Map<String,Object>>();
		matches.forEach(match -> list.add(match.getParameters()));
		
		var map = new HashMap<String,Object>();
		map.put("matches",(Object)list);
		
		logger.debug(map.toString() + "\n" + cypherQuery);
		var result = builder.executeQueryWithParameters(cypherQuery, map);

		var results = result.list();
		
		if(results.size()==1 && results.get(0).size()==1) {
			var returnMap = new HashMap<String,Boolean>();
			for(var match : matches) {
				returnMap.put(match.getHashCode(),results.get(0).get(0).asList().contains(match.getHashCode()));
			}
			logger.debug(returnMap.toString());
			return returnMap;
		} else {
			throw new IllegalStateException("There should be at most one record found not " + results.size());
		}	
	}

	@Override
	public String getQuery() {
		var condData = referencedConstraint.getConditionData();
		return CypherPatternBuilder.conditionQuery_copyPaste(getNodes(), condData.getOptionalMatchString(),
				condData.getWhereClause(), queryData.getAllElements(), isNegated, queryData.getAttributeExpressions(), injective, 0);
	}
}
