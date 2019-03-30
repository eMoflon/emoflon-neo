package org.emoflon.neo.example.sokoban.scalability;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;

/**
 * An example of unit testing with Neo4j.
 */
public class Neo4jBasicDocTest {
	protected GraphDatabaseService graphDb;

	/**
	 * Create temporary database for each unit test.
	 */
	@Before
	public void prepareTestDatabase() {
		graphDb = new TestGraphDatabaseFactory().newImpermanentDatabase();
	}

	/**
	 * Shutdown the database.
	 */
	@After
	public void destroyTestDatabase() {
		if (graphDb != null)
			graphDb.shutdown();
	}

	@Test
	public void shouldCreateNode() {
		String createGraph = "CREATE\n" +
	            "(ben:User {name:'Ben'}),\n" +
	            "(arnold:User {name:'Arnold'}),\n" +
	            "(charlie:User {name:'Charlie'}),\n" +
	            "(gordon:User {name:'Gordon'}),\n" +
	            "(lucy:User {name:'Lucy'}),\n" +
	            "(emily:User {name:'Emily'}),\n" +
	            "(sarah:User {name:'Sarah'}),\n" +
	            "(kate:User {name:'Kate'}),\n" +
	            "(mike:User {name:'Mike'}),\n" +
	            "(paula:User {name:'Paula'}),\n" +
	            "(ben)-[:FRIEND]->(charlie),\n" +
	            "(charlie)-[:FRIEND]->(lucy),\n" +
	            "(lucy)-[:FRIEND]->(sarah),\n" +
	            "(sarah)-[:FRIEND]->(mike),\n" +
	            "(arnold)-[:FRIEND]->(gordon),\n" +
	            "(gordon)-[:FRIEND]->(emily),\n" +
	            "(emily)-[:FRIEND]->(kate),\n" +
	            "(kate)-[:FRIEND]->(paula)";
		
		graphDb.execute(createGraph);
	}
}