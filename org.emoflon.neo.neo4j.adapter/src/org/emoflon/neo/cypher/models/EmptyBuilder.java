package org.emoflon.neo.cypher.models;

import java.util.List;
import java.util.Map;

import org.neo4j.driver.Record;

public class EmptyBuilder implements IBuilder {

	@Override
	public List<Record> executeQuery(String cypherQuery) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Record> executeQuery(String cypherQuery, Map<String, Object> params) {
		throw new UnsupportedOperationException();
	}

}
