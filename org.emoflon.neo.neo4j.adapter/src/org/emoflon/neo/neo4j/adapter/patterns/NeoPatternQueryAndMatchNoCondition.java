package org.emoflon.neo.neo4j.adapter.patterns;

import java.util.ArrayList;
import java.util.Collection;

import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.neo4j.adapter.CypherPatternBuilder;
import org.emoflon.neo.neo4j.adapter.IBuilder;
import org.emoflon.neo.neo4j.adapter.NeoMask;
import org.emoflon.neo.neo4j.adapter.NeoMatch;

public class NeoPatternQueryAndMatchNoCondition extends NeoPattern {

	public NeoPatternQueryAndMatchNoCondition(Pattern p, IBuilder builder, NeoMask mask) {
		super(p, builder, mask);
	}

	@Override
	public String getQuery() {
		return CypherPatternBuilder.readQuery_copyPaste(nodes, injective);
	}

	@Override
	public Collection<NeoMatch> determineMatches(int limit) {
		logger.info("Searching matches for Pattern: " + getName());
		var cypherQuery = "";
		if (limit > 0)
			cypherQuery = CypherPatternBuilder.readQuery(nodes, injective, limit);
		else
			cypherQuery = CypherPatternBuilder.readQuery(nodes, injective);
		logger.debug(cypherQuery);

		var result = builder.executeQuery(cypherQuery);

		var matches = new ArrayList<NeoMatch>();
		while (result.hasNext()) {
			var record = result.next();
			matches.add(new NeoMatch(this, record));
		}

		if (matches.isEmpty()) {
			logger.debug("NO MATCHES FOUND");
		}
		return matches;
	}

	@Override
	public boolean isStillValid(NeoMatch m) {
		logger.info("Check if match for " + getName() + " is still valid");
		var cypherQuery = CypherPatternBuilder.isStillValidQuery(nodes, m, injective);
		logger.debug(cypherQuery);
		var result = builder.executeQuery(cypherQuery);

		// Query is id-based and must be unique
		var results = result.list();
		if (results.size() > 1) {
			throw new IllegalStateException("There should be at most one record found not " + results.size());
		}

		return results.size() == 1;
	}

}
