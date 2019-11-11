
package org.emoflon.neo.cypher.models;

import java.util.Map;
import java.util.Set;

import org.emoflon.neo.cypher.models.templates.CypherBuilder;
import org.neo4j.driver.v1.StatementResult;

public interface IBuilder {
	StatementResult executeQuery(String cypherQuery);

	StatementResult executeQuery(String cypherQuery, Map<String, Object> params);

	static IBuilder empty() {
		return new EmptyBuilder();
	}

	default void deleteAll(Set<Long> corrs) {
		Map<String, Object> params = Map.of("ids", corrs);
		executeQuery(CypherBuilder.deleteCorrQuery("ids"), params);
	}
}
