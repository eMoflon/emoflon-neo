package org.emoflon.neo.example;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Scanner;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.emsl.eMSL.Model;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.api.rules.IPattern;
import org.emoflon.neo.neo4j.adapter.NeoCoreBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.neo4j.driver.v1.StatementResult;

public abstract class ENeoTest {
	
	private static Scanner reader;
	protected static final Logger logger = Logger.getLogger(ENeoTest.class);
	protected static NeoCoreBuilder builder; 
	
	@BeforeAll
	public static void startDBConnection() throws Exception {
		Logger.getRootLogger().setLevel(Level.DEBUG);
		
		logger.info("Database Connection established.");
		builder = API_Common.createBuilder();
		StatementResult result = builder.executeQuery("MATCH (n) RETURN count(n)");
	
		if (result.hasNext()) {

			if (result.next().get(0).asInt() > 0) {
				logger.info(
						"Database not empty. All data will be removed! \n" + "Do you want to continue (Y=Yes / N=No)?");
				reader = new Scanner(System.in);
				String input = reader.next();

				if (input.toLowerCase().equals("n")) {
					logger.info("Tests have been canceled. No changes in the database executed.");
					closeDBConnection();
					fail();
				} else {
					builder.executeQueryForSideEffect("MATCH (n) DETACH DELETE n");
					logger.info("Database cleared.");
				}
			} else {
				logger.info("Database empty.");
			}

		} else {
			logger.info("Database empty.");
		}
	}

	protected static void initDB(Model model) {
		builder.exportEMSLEntityToNeo4j(model);
		logger.info("-----------------------------\n" + "Database initialised.");
	}
	
	@AfterAll
	public static void closeDBConnection() throws Exception {
		builder.close();
		logger.info("Database Connection closed.");
	}
	
	@AfterEach
	public void clearDB() {
		builder.executeQueryForSideEffect("MATCH (n) DETACH DELETE n");
		logger.info("Database cleared.");
	}
	
	protected void expectSingleMatch(IPattern p) {
		assertThat(p.countMatches(), is(1));
	}
	
	protected void expectNoMatch(IPattern p) {
		assertThat(p.countMatches(), is(0));
	}
	
	protected void expectValidMatches(Collection<IMatch> matches, long number) {
		assertEquals(matches.stream().filter(IMatch::isStillValid).count(), number);
	}
}
