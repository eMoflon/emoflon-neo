package org.emoflon.neo.neo4j.adapter.patterns;

import java.util.ArrayList;
import java.util.Collection;

import org.emoflon.neo.emsl.eMSL.ConstraintReference;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.neo4j.adapter.constraints.NeoConstraint;
import org.emoflon.neo.neo4j.adapter.constraints.NeoConstraintFactory;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.templates.CypherPatternBuilder;

public class NeoPatternQueryAndMatchConstraintRef extends NeoPattern {
	protected NeoConstraint referencedConstraint;

	public NeoPatternQueryAndMatchConstraintRef(Pattern p, IBuilder builder, NeoMask mask) {
		super(p, builder, mask);
		ConstraintReference ref = (ConstraintReference) p.getCondition();
		referencedConstraint = NeoConstraintFactory.createNeoConstraint(ref.getReference(), builder, helper, mask);
	}

	@Override
	public Collection<NeoMatch> determineMatches(int limit) {
		logger.info("Searching matches for Pattern: " + getName() + " WHEN " + referencedConstraint.getName());

		// collecting the data
		var condData = referencedConstraint.getConditionData();

		// creating the query string
		var cypherQuery = CypherPatternBuilder.conditionQuery(getNodes(), condData.getOptionalMatchString(),
				condData.getWhereClause(), helper.getAllElements(), isNegated(), limit);
		logger.debug(cypherQuery);

		// run the query
		var result = builder.executeQuery(cypherQuery);

		// analyze and return results
		var matches = new ArrayList<NeoMatch>();
		while (result.hasNext()) {
			var record = result.next();
			matches.add(new NeoMatch(this, record));
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
	@Override
	public boolean isStillValid(NeoMatch m) {
		logger.info("Check if match for " + getName() + " WHEN " + referencedConstraint.getName() + " is still valid");

		// collecting the data
		var condData = referencedConstraint.getConditionData();

		// creating the query string
		var cypherQuery = CypherPatternBuilder.conditionQuery_isStillValid(getNodes(),
				condData.getOptionalMatchString(), condData.getWhereClause(), helper.getAllElements(), isNegated(), m);
		logger.debug(cypherQuery);

		// run the query
		var result = builder.executeQuery(cypherQuery);

		// analyze and return results
		var matches = new ArrayList<NeoMatch>();
		while (result.hasNext()) {
			var record = result.next();
			matches.add(new NeoMatch(this, record));
		}

		return matches.size() == 1;
	}

	@Override
	public String getQuery() {
		var condData = referencedConstraint.getConditionData();
		return CypherPatternBuilder.conditionQuery_copyPaste(getNodes(), condData.getOptionalMatchString(),
				condData.getWhereClause(), helper.getAllElements(), isNegated(), 0);
	}
}
