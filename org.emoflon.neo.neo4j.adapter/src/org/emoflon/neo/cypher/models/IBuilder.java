
package org.emoflon.neo.cypher.models;

import java.util.List;
import java.util.Map;

import org.neo4j.driver.Record;

public interface IBuilder {
	List<Record> executeQuery(String cypherQuery);

	List<Record> executeQuery(String cypherQuery, Map<String, Object> params);

	static IBuilder empty() {
		return new EmptyBuilder();
	}
}
