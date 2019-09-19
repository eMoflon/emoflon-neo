package org.emoflon.neo.neo4j.adapter.models;

import java.util.Map;

import org.neo4j.driver.v1.StatementResult;

public class EmptyBuilder implements IBuilder {

	@Override
	public StatementResult executeQuery(String cypherQuery) {
		throw new UnsupportedOperationException();
	}

	@Override
	public StatementResult executeQueryWithParameters(String cypherQuery, Map<String, Object> params) {
		throw new UnsupportedOperationException();
	}

}
