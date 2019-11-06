package org.emoflon.neo.cypher.common;

import org.neo4j.driver.v1.exceptions.DatabaseException;

public class NeoDatabaseException extends DatabaseException {

	public NeoDatabaseException() {
		super("400", "Execution Error: See console log for more details.");
	}

	private static final long serialVersionUID = 1L;
}
