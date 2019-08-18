package org.emoflon.neo.neo4j.adapter.models;

import org.neo4j.driver.v1.StatementResult;

public class EmptyBuilder implements IBuilder {

	@Override
	public StatementResult executeQuery(String cypherQuery) {
		throw new UnsupportedOperationException();
	}

}
