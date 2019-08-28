package org.emoflon.neo.neo4j.adapter.patterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
				ncond.getQueryString_MatchCondition(), ncond.getQueryString_WhereCondition(), injective, limit, mask);
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
				ncond.getQueryString_MatchCondition(), ncond.getQueryString_WhereCondition(), injective, m);

		logger.debug(cypherQuery);

		// Execute query
		var result = builder.executeQuery(cypherQuery);
		
		if(result == null) {
			throw new DatabaseException("400", "Execution Error: See console log for more details.");
		} else {
			return result.list().size() == 1;
		}
	}

}
