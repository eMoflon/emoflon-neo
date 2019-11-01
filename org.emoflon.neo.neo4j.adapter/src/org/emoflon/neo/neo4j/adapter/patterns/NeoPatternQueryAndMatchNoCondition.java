package org.emoflon.neo.neo4j.adapter.patterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.emoflon.neo.cypher.models.IBuilder;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.engine.generator.Schedule;
import org.emoflon.neo.neo4j.adapter.templates.CypherPatternBuilder;
import org.emoflon.neo.neo4j.adapter.util.NeoQueryData;
import org.neo4j.driver.v1.exceptions.DatabaseException;

public class NeoPatternQueryAndMatchNoCondition extends NeoPattern {
	private static final Logger logger = Logger.getLogger(NeoPatternQueryAndMatchNoCondition.class);
	
	public NeoPatternQueryAndMatchNoCondition(List<ModelNodeBlock> nodeBlocks, String name, IBuilder builder, NeoMask mask, NeoQueryData queryData) {
		super(nodeBlocks, name, builder, mask, queryData);
	}

	@Override
	public String getQuery() {
		return CypherPatternBuilder.readQuery_copyPaste(nodes, queryData.getAttributeExpressions(), injective);
	}

	@Override
	public Collection<NeoMatch> determineMatches(Schedule schedule) {
		logger.debug("Searching matches for Pattern: " + getName());
		var cypherQuery = CypherPatternBuilder.readQuery(nodes, queryData.getAttributeExpressions(), injective, schedule, mask);
		logger.debug(cypherQuery);

		var result = builder.executeQuery(cypherQuery, schedule.getParameters());
		var matches = new ArrayList<NeoMatch>();
		
		if(result == null) {
			throw new DatabaseException("400", "Execution Error: See console log for more details.");
		} else {
			
			while (result.hasNext()) {
				matches.add(new NeoMatch(this, result.next()));
			}
			
			if (matches.isEmpty()) {
				logger.debug("NO MATCHES FOUND");
			} else {
				logger.debug(matches.size() + " MATCHES FOUND");
			}
			return matches;	
		}
	}
	
	@Override
	public Map<String,Boolean> isStillValid(Collection<NeoMatch> matches) {
		logger.debug("Check if matches for " + getName() + " are still valid");
		var cypherQuery = CypherPatternBuilder.isStillValidQuery(nodes, queryData.getAttributeExpressions(), injective);
		
		var list = new ArrayList<Map<String,Object>>();
		matches.forEach(match -> list.add(match.getParameters()));
		
		var map = new HashMap<String,Object>();
		map.put("matches",list);
		
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
}
