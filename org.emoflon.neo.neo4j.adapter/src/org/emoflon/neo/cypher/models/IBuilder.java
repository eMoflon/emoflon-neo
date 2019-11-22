
package org.emoflon.neo.cypher.models;

import java.util.Map;

import org.neo4j.driver.v1.StatementResult;

public interface IBuilder {
	StatementResult executeQuery(String cypherQuery);

	StatementResult executeQuery(String cypherQuery, Map<String, Object> params);

	static IBuilder empty() {
		return new EmptyBuilder();
	}
}
