package org.emoflon.neo.neo4j.adapter.patterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.templates.CypherPatternBuilder;
import org.emoflon.neo.neo4j.adapter.util.NeoQueryData;
import org.neo4j.driver.v1.exceptions.DatabaseException;

public class NeoPatternQueryAndMatchNoCondition extends NeoPattern {

	public NeoPatternQueryAndMatchNoCondition(List<ModelNodeBlock> nodeBlocks, String name, IBuilder builder, NeoMask mask, NeoQueryData queryData) {
		super(nodeBlocks, name, builder, mask, queryData);
	}

	@Override
	public String getQuery() {
		return CypherPatternBuilder.readQuery_copyPaste(nodes, queryData.getAttributeExpressions(), injective);
	}

	@Override
	public Collection<NeoMatch> determineMatches(int limit) {
		logger.info("Searching matches for Pattern: " + getName());
		var cypherQuery = CypherPatternBuilder.readQuery(nodes, queryData.getAttributeExpressions(), injective, limit, mask);
		logger.debug(cypherQuery);

		var result = builder.executeQuery(cypherQuery);
		var matches = new ArrayList<NeoMatch>();
		
		if(result == null) {
			throw new DatabaseException("400", "Execution Error: See console log for more details.");
		} else {
			
			while (result.hasNext()) {
				var record = result.next();
				matches.add(new NeoMatch(this, record));
			}
			
			if (matches.isEmpty()) {
				logger.info("NO MATCHES FOUND");
			} else {
				logger.info(matches.size() + " MATCHES FOUND");
			}
			return matches;	
		}
	}

	@Override
	public boolean isStillValid(NeoMatch m) {
		logger.info("Check if match for " + getName() + " is still valid");
		var cypherQuery = CypherPatternBuilder.isStillValidQuery(nodes, queryData.getAttributeExpressions(), injective);
		
		logger.debug(m.getParameters().toString() + "\n" + cypherQuery);
		var result = builder.executeQueryWithParameters(cypherQuery, m.getParameters());

		if(result == null) {
			throw new DatabaseException("400", "Execution Error: See console log for more details.");
		} else {
			// Query is id-based and must be unique
			var results = result.list();
			if (results.size() > 1) {
				throw new IllegalStateException("There should be at most one record found not " + results.size());
			}
	
			return results.size() == 1;
		}
	}
}
