package org.emoflon.neo.neo4j.adapter.patterns;

import java.util.ArrayList;
import java.util.Collection;

import org.emoflon.neo.emsl.eMSL.NegativeConstraint;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.neo4j.adapter.constraints.NeoNegativeConstraint;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.templates.CypherPatternBuilder;
import org.emoflon.neo.neo4j.adapter.util.NeoQueryData;

public class NeoPatternQueryAndMatchNegativeConstraint extends NeoPattern {
	protected NeoNegativeConstraint ncond;

	public NeoPatternQueryAndMatchNegativeConstraint(Pattern p, IBuilder builder, NeoMask mask, NeoQueryData queryData) {
		super(p, builder, mask, queryData);
		var cons = (NegativeConstraint) p.getCondition();
		ncond = new NeoNegativeConstraint(cons.getPattern(), injective, builder, queryData, mask);
	}

	@Override
	public String getQuery() {
		return getQuery(ncond.getQueryString_MatchCondition(), ncond.getQueryString_WhereCondition());
	}

	@Override
	public Collection<NeoMatch> determineMatches(int limit) {
		// Condition is negative Constraint (FORBID xyz)
		logger.info("Searching matches for Pattern: " + p.getBody().getName() + " FORBID " + ncond.getName());

		// create query
		var cypherQuery = CypherPatternBuilder.constraintQuery(nodes, queryData.getAllElements(),
				ncond.getQueryString_MatchCondition(), ncond.getQueryString_WhereCondition(), injective, limit, mask);
		logger.debug(cypherQuery);

		// execute query
		var result = builder.executeQuery(cypherQuery);

		// analyze and return results
		var matches = new ArrayList<NeoMatch>();
		while (result.hasNext()) {
			var record = result.next();
			matches.add(new NeoMatch(this, record));
		}

		return matches;
	}

	@Override
	public boolean isStillValid(NeoMatch m) {
		// Condition is positive Constraint (ENFORCE xyz)
		logger.info("Check if match for " + p.getBody().getName() + " WHEN " + ncond.getName() + " is still valid");

		// Create Query
		var cypherQuery = CypherPatternBuilder.constraintQuery_isStillValid(nodes, queryData.getAllElements(),
				ncond.getQueryString_MatchCondition(), ncond.getQueryString_WhereCondition(), injective, m);

		logger.debug(cypherQuery);

		// Execute query
		var result = builder.executeQuery(cypherQuery);
		return result.hasNext();
	}

}
