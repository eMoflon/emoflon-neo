package org.emoflon.neo.neo4j.adapter;

import org.neo4j.driver.v1.StatementResult;

public interface IBuilder {
	StatementResult executeQuery(String cypherQuery);
}
