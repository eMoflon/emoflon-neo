package org.emoflon.neo.neo4j.adapter.rules;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.emoflon.neo.neo4j.adapter.constraints.NeoConstraint;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.models.NeoCoreBuilder;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.templates.CypherPatternBuilder;
import org.emoflon.neo.neo4j.adapter.util.NeoQueryData;

/**
 * Class created, when a pattern has a condition. Runs relevant pattern and
 * constraint matching checks
 * 
 * @author Jannik Hinz
 *
 */
public class NeoCondition {
	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	private IBuilder builder;
	private NeoQueryData queryData;
	private NeoConstraint c;
	private NeoRule r;

	public NeoCondition(NeoConstraint c, NeoRule r, String name, IBuilder builder, NeoQueryData queryData) {
		this.builder = builder;
		this.queryData = queryData;
		this.c = c;
		this.r = r;
	}

	public Collection<NeoMatch> determineMatchesRule(int limit) {
		logger.info("Searching matches for Rule: " + r.getName() + " WHEN " + c.getName());

		// collecting the data
		var condData = c.getConditionData();

		// creating the query string
		var cypherQuery = CypherPatternBuilder.conditionQuery(r.getNodes(), condData.getOptionalMatchString(),
				condData.getWhereClause(), queryData.getAllElements(), r.isNegated(), limit);
		logger.debug(cypherQuery);

		// run the query
		var result = builder.executeQuery(cypherQuery);

		// analyze and return results
		var matches = new ArrayList<NeoMatch>();
		while (result.hasNext()) {
			var record = result.next();
			matches.add(new NeoMatch(r, record));
		}

		return matches;
	}

	/**
	 * Get the data and nodes from the (nested) conditions and runs the query in the
	 * database, analyze the results and return the matches
	 * 
	 * @param limit number of matches, that should be returned - 0 if infinite
	 * @return Collection<IMatch> return a list of all Matches of the pattern with
	 *         condition matching
	 */
	public boolean isStillValid(NeoMatch m) {
		logger.info("Check if match for " + r.getName() + " WHEN " + c.getName() + " is still valid");

		// collecting the data
		var condData = c.getConditionData();

		// creating the query string
		var cypherQuery = CypherPatternBuilder.conditionQuery_isStillValid(r.getNodes(),
				condData.getOptionalMatchString(), condData.getWhereClause(), queryData.getAllElements(), r.isNegated(),
				m);
		logger.debug(cypherQuery);

		// run the query
		var result = builder.executeQuery(cypherQuery);

		// analyze and return results
		var matches = new ArrayList<NeoMatch>();
		while (result.hasNext()) {
			var record = result.next();
			matches.add(new NeoMatch(r, record));
		}

		return matches.size() == 1;
	}
}
