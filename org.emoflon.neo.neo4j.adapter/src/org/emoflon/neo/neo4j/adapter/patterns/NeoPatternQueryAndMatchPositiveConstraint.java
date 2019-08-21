package org.emoflon.neo.neo4j.adapter.patterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.PositiveConstraint;
import org.emoflon.neo.neo4j.adapter.constraints.NeoPositiveConstraint;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.templates.CypherPatternBuilder;
import org.emoflon.neo.neo4j.adapter.util.NeoQueryData;

public class NeoPatternQueryAndMatchPositiveConstraint extends NeoPattern {

	protected NeoPositiveConstraint pcond;

	@Override
	public String getQuery() {
		return getQuery(pcond.getQueryString_MatchCondition(), pcond.getQueryString_WhereCondition());
	}

	public NeoPatternQueryAndMatchPositiveConstraint(List<ModelNodeBlock> nodeBlocks, String name,
			PositiveConstraint pconstr, IBuilder builder, NeoMask mask, NeoQueryData queryData) {
		super(nodeBlocks, name, builder, mask, queryData);
		pcond = new NeoPositiveConstraint(pconstr.getPattern(), injective, builder, queryData, mask);
	}

	@Override
	public Collection<NeoMatch> determineMatches(int limit) {
		// Condition is positive Constraint (ENFORCE xyz)
		logger.info("Searching matches for Pattern: " + getName() + " ENFORCE " + pcond.getName());

		// Create Query
		var cypherQuery = CypherPatternBuilder.constraintQuery(nodes, queryData.getAllElements(),
				pcond.getQueryString_MatchCondition(), pcond.getQueryString_WhereCondition(), injective, limit, mask);

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
		logger.info("Check if match for " + getName() + " WHEN " + pcond.getName() + " is still valid");

		// Create Query
		var cypherQuery = CypherPatternBuilder.constraintQuery_isStillValid(nodes, queryData.getAllElements(),
				pcond.getQueryString_MatchCondition(), pcond.getQueryString_WhereCondition(), injective, m);

		logger.debug(cypherQuery);

		// Execute query
		var result = builder.executeQuery(cypherQuery);
		return result.hasNext();
	}

}
