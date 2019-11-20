package org.emoflon.neo.example;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.patterns.NeoPatternAccess;
import org.emoflon.neo.emsl.eMSL.Model;
import org.emoflon.neo.emsl.util.FlattenerException;
import org.emoflon.neo.engine.modules.ilp.ILPBasedOperationalStrategy;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.neo4j.driver.v1.StatementResult;

import run.CompanyToIT_CC_Run;
import run.CompanyToIT_CO_Run;

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
					builder.clearDataBase();;
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
		try {
			builder.exportEMSLEntityToNeo4j(model);
			logger.info("-----------------------------\n" + "Database initialised.");
		} catch (FlattenerException e) {
			e.printStackTrace();
		}
	}
	
	@AfterAll
	public static void closeDBConnection() throws Exception {
		builder.close();
		logger.info("Database Connection closed.");
	}
	
	@AfterEach
	public void clearDB() {
		builder.clearDataBase();
		logger.info("Database cleared.");
	}
	
	
	protected void expectMatches(NeoPatternAccess<?,?> p, Number no) {
		assertThat(p.pattern().countMatches(), is(no));
	}
	
	protected void expectSingleMatch(NeoPatternAccess<?,?> p) {
		expectMatches(p, 1);
	}
	
	protected void expectNoMatch(NeoPatternAccess<?,?> p) {
		expectMatches(p, 0);
	}
	
	protected void expectValidMatches(Collection<NeoMatch> matches, long number) {
		var pattern = matches.iterator().next().getPattern();
		var isStillValid = pattern.isStillValid(matches);
		assertEquals(number, isStillValid.values().stream().filter(res -> res == true).count());
	}
	
	protected void expectValidMatch(NeoMatch m) {
		expectValidMatches(List.of(m), 1);
	}
	
	protected void expectInvalidMatch(NeoMatch m) {
		expectValidMatches(List.of(m), 0);
	}
	
	private void testForConsistency(ILPBasedOperationalStrategy result, int numberOfConsistentElements) throws Exception {
		assertTrue(result.isConsistent());
		assertEquals(0, result.determineInconsistentElements().size());
		assertEquals(numberOfConsistentElements, result.determineConsistentElements().size());
	}
	
	protected void testConsistentTripleCO(Model triple, int numberOfConsistentElements) throws Exception {
		var testCOApp = new CompanyToIT_CO_Run();
		builder.exportEMSLEntityToNeo4j(triple);
		var result = testCOApp.runCheckOnly();
		testForConsistency(result, numberOfConsistentElements);
	}

	protected void testConsistentTripleCC(Model triple, int numberOfConsistentElements) throws Exception {
		var testCCApp = new CompanyToIT_CC_Run();
		builder.exportEMSLEntityToNeo4j(triple);
		builder.deleteAllCorrs();
		var result = testCCApp.runCorrCreation(); 
		testForConsistency(result, numberOfConsistentElements);
	}

	private void testForInconsistency(ILPBasedOperationalStrategy result, int consistent, int inconsistent) throws Exception {
		assertFalse(result.isConsistent());
		assertEquals(inconsistent, result.determineInconsistentElements().size());
		assertEquals(consistent, result.determineConsistentElements().size());
	}
	
	protected void testInconsistentTripleCO(Model triple, int consistent, int inconsistent) throws Exception {
		var testCOApp = new CompanyToIT_CO_Run();
		builder.exportEMSLEntityToNeo4j(triple);
		var result = testCOApp.runCheckOnly();
		testForInconsistency(result, consistent, inconsistent);
	}
	
	protected void testInconsistentTripleCC(Model triple, int consistent, int inconsistent) throws Exception {
		var testCCApp = new CompanyToIT_CC_Run();
		builder.exportEMSLEntityToNeo4j(triple);
		builder.deleteAllCorrs();
		var result = testCCApp.runCorrCreation();
		testForInconsistency(result, consistent, inconsistent);
	}
}
