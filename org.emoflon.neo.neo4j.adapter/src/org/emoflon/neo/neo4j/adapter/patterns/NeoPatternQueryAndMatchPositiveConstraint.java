package org.emoflon.neo.neo4j.adapter.patterns;

import java.util.ArrayList;
import java.util.Collection;

import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.eMSL.PositiveConstraint;
import org.emoflon.neo.neo4j.adapter.CypherPatternBuilder;
import org.emoflon.neo.neo4j.adapter.IBuilder;
import org.emoflon.neo.neo4j.adapter.NeoMask;
import org.emoflon.neo.neo4j.adapter.NeoMatch;
import org.emoflon.neo.neo4j.adapter.NeoPositiveConstraint;

public class NeoPatternQueryAndMatchPositiveConstraint extends NeoPattern {

	protected NeoPositiveConstraint pcond;

	@Override
	public String getQuery() {
		return getQuery(pcond.getQueryString_MatchCondition(), pcond.getQueryString_WhereConditon());
	}

	public NeoPatternQueryAndMatchPositiveConstraint(Pattern p, IBuilder builder, NeoMask mask) {
		super(p, builder, mask);
		PositiveConstraint cons = (PositiveConstraint) p.getCondition();
		pcond = new NeoPositiveConstraint(cons.getPattern(), injective, builder, helper, mask);
	}

	@Override
	public Collection<NeoMatch> determineMatches(int limit) {
		// Condition is positive Constraint (ENFORCE xyz)
		logger.info("Searching matches for Pattern: " + p.getBody().getName() + " ENFORCE " + pcond.getName());

		// Create Query
		var cypherQuery = CypherPatternBuilder.constraintQuery(nodes, helper.getNodes(),
				pcond.getQueryString_MatchCondition(), pcond.getQueryString_WhereConditon(), injective, limit, mask);

		logger.debug(cypherQuery);

		// Execute query
		var result = builder.executeQuery(cypherQuery);

		// Analyze and return results
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
		logger.info("Check if match for " + p.getBody().getName() + " WHEN " + pcond.getName() + " is still valid");

		// Create Query
		var cypherQuery = CypherPatternBuilder.constraintQuery_isStillValid(nodes, helper.getNodes(),
				pcond.getQueryString_MatchCondition(), pcond.getQueryString_WhereConditon(), injective, m);

		logger.debug(cypherQuery);

		// Execute query
		var result = builder.executeQuery(cypherQuery);
		return result.hasNext();
	}

}
