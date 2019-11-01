package org.emoflon.neo.neo4j.adapter.patterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.ConstraintReference;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.engine.generator.Schedule;
import org.emoflon.neo.neo4j.adapter.constraints.NeoConstraint;
import org.emoflon.neo.neo4j.adapter.constraints.NeoConstraintFactory;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.templates.CypherPatternBuilder;
import org.emoflon.neo.neo4j.adapter.util.NeoQueryData;
import org.neo4j.driver.v1.exceptions.DatabaseException;

public class NeoPatternQueryAndMatchConstraintRef extends NeoPattern {
	private static final Logger logger = Logger.getLogger(NeoPatternQueryAndMatchConstraintRef.class);
	
	protected NeoConstraint referencedConstraint;
	protected boolean isNegated;

	public NeoPatternQueryAndMatchConstraintRef(List<ModelNodeBlock> nodeBlocks, String name, ConstraintReference ref, IBuilder builder, NeoMask mask, NeoQueryData queryData) {
		super(nodeBlocks, name, builder, mask, queryData);
		isNegated = ref.isNegated();
		referencedConstraint = NeoConstraintFactory.createNeoConstraint(ref.getReference(), builder, queryData, mask);
	}

	@Override
	public Collection<NeoMatch> determineMatches(Schedule schedule) {
		logger.debug("Searching matches for Pattern: " + getName() + " WHEN " + referencedConstraint.getName());

		// collecting the data
		var condData = referencedConstraint.getConditionData();

		// creating the query string
		var cypherQuery = CypherPatternBuilder.conditionQuery(getNodes(), condData.getOptionalMatchString(),
				condData.getWhereClause(), queryData.getAllElements(), isNegated, queryData.getAttributeExpressions(), injective, schedule, mask);
		logger.debug(cypherQuery);

		// run the query
		var result = builder.executeQuery(cypherQuery, schedule.getParameters());

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
	public Map<String,Boolean> isStillValid(Collection<NeoMatch> matches) {
		
		logger.debug("Check if matches for " + getName() + " WHEN " + referencedConstraint.getName() + " is still valid");

		// collecting the data
		var condData = referencedConstraint.getConditionData();
		
		var list = new ArrayList<Map<String,Object>>();
		matches.forEach(match -> list.add(match.getParameters()));
		
		var map = new HashMap<String,Object>();
		map.put("matches", list);
		
		// Create Query
		var cypherQuery = CypherPatternBuilder.conditionQuery_isStillValid(getNodes(),
				condData.getOptionalMatchString(), condData.getWhereClause(), queryData.getAllElements(), queryData.getAttributeExpressions(), isNegated);

		logger.debug(map.toString() + "\n" + cypherQuery);
		var result = builder.executeQuery(cypherQuery, map);

		var results = result.list();
		var hashCode = new ArrayList<String>();
		for(var r : results) {
			hashCode.add(r.asMap().get("match_id").toString());
		}
		
		var returnMap = new HashMap<String,Boolean>();
		for(var match : matches) {
			returnMap.put(match.getHashCode(),hashCode.contains(match.getHashCode()));
		}
		
		logger.debug(returnMap.toString());
		return returnMap;	
	}

	@Override
	public String getQuery() {
		var condData = referencedConstraint.getConditionData();
		return CypherPatternBuilder.conditionQuery_copyPaste(//
				getNodes(), //
				condData.getOptionalMatchString(), //
				condData.getWhereClause(), //
				queryData.getAllElements(), //
				isNegated, //
				queryData.getAttributeExpressions(), //
				injective, //
				Schedule.unlimited());
	}
}
