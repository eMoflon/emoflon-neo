package org.emoflon.neo.neo4j.adapter.patterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.NegativeConstraint;
import org.emoflon.neo.neo4j.adapter.constraints.NeoNegativeConstraint;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.templates.CypherPatternBuilder;
import org.emoflon.neo.neo4j.adapter.util.NeoQueryData;
import org.neo4j.driver.v1.exceptions.DatabaseException;

public class NeoPatternQueryAndMatchNegativeConstraint extends NeoPattern {
	protected NeoNegativeConstraint ncond;

	public NeoPatternQueryAndMatchNegativeConstraint(List<ModelNodeBlock> nodeBlocks, String name,
			NegativeConstraint nconstr, IBuilder builder, NeoMask mask, NeoQueryData queryData) {
		super(nodeBlocks, name, builder, mask, queryData);
		ncond = new NeoNegativeConstraint(nconstr.getPattern(), injective, builder, queryData, mask);
	}

	@Override
	public String getQuery() {
		return getQuery(ncond.getQueryString_MatchCondition(), ncond.getQueryString_WhereCondition());
	}

	@Override
	public Collection<NeoMatch> determineMatches(int limit) {
		// Condition is negative Constraint (FORBID xyz)
		logger.info("Searching matches for Pattern: " + getName() + " FORBID " + ncond.getName());

		// create query
		var cypherQuery = CypherPatternBuilder.constraintQuery(nodes, queryData.getAllElements(),
				ncond.getQueryString_MatchCondition(), ncond.getQueryString_WhereCondition(), queryData.getAttributeExpressions(), injective, limit, mask);
		logger.debug(cypherQuery);

		// execute query
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

	@Override
	public boolean isStillValid(NeoMatch m) {
		// Condition is positive Constraint (ENFORCE xyz)
		logger.info("Check if match for " + getName() + " WHEN " + ncond.getName() + " is still valid");

		// Create Query
		var cypherQuery = CypherPatternBuilder.constraintQuery_isStillValid(nodes, queryData.getAllElements(),
				ncond.getQueryString_MatchCondition(), ncond.getQueryString_WhereCondition(), queryData.getAttributeExpressions(), injective);

		logger.debug(m.getParameters().toString() + "\n" + cypherQuery);

		// Execute query
		var result = builder.executeQueryWithParameters(cypherQuery, m.getParameters());
		
		if(result == null) {
			throw new DatabaseException("400", "Execution Error: See console log for more details.");
		} else {
			return result.list().size() == 1;
		}
	}
	
	@Override
	public Map<String,Boolean> isStillValid(Collection<NeoMatch> matches) {
		
		// Condition is positive Constraint (ENFORCE xyz)
		logger.info("Check if match for " + getName() + " WHEN " + ncond.getName() + " is still valid");
		
		var list = new ArrayList<Map<String,Object>>();
		matches.forEach(match -> list.add(match.getParameters()));
		
		var map = new HashMap<String,Object>();
		map.put("matches",(Object)list);
		
		// Create Query
		var helperNodes = new ArrayList<String>(queryData.getAllElements());
		helperNodes.add("matches");
		
		var cypherQuery = CypherPatternBuilder.constraintQuery_isStillValidCollection(nodes, helperNodes,
				ncond.getQueryString_MatchCondition(), ncond.getQueryString_WhereCondition(), queryData.getAttributeExpressions(), injective);

		logger.debug(map.toString() + "\n" + cypherQuery);
		var result = builder.executeQueryWithParameters(cypherQuery, map);

		logger.info(result.toString());
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
}
