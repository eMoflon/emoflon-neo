package org.emoflon.neo.example.sokoban.patterns;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Scanner;

import org.apache.log4j.Logger;
import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.emsl.eMSL.EMSL_Spec;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.util.EMSUtil;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.api.rules.IPattern;
import org.emoflon.neo.example.sokoban.scalability.ScalabilityTest;
import org.emoflon.neo.neo4j.adapter.NeoCoreBuilder;
import org.emoflon.neo.neo4j.adapter.NeoPattern;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.StatementResult;

public class PatternTest {

	private static Scanner reader;
	private static final Logger logger = Logger.getLogger(ScalabilityTest.class);
	private static NeoCoreBuilder builder = API_Common.createBuilder();
	private static Driver driver = builder.getDriver();

	// Select model
	private EMSL_Spec model = EMSUtil.loadSpecification(//
			"platform:/resource/SokobanLanguage/models/SokobanSimpleTestField.msl", //
			"../");

	// Select pattern
	private EMSL_Spec patterns = EMSUtil.loadSpecification(//
			"platform:/resource/SokobanLanguage/rules/SokobanPatternsRulesConstraints.msl", //
			"../");

	@BeforeAll
	private static void startDBConnection() throws Exception {
		logger.info("Database Connection established.");
		StatementResult result = driver.session().run("MATCH (n) RETURN count(n)");

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
					driver.session().run("MATCH (n) DETACH DELETE n");
					logger.info("Database cleared.");
				}
			} else {
				logger.info("Database empty.");
			}

		} else {
			logger.info("Database empty.");
		}

	}

	@BeforeEach
	private void initDB() {
		builder.exportEMSLEntityToNeo4j(model);
		logger.info("-----------------------------\n" + "Database initialised.");
	}
	
	@AfterEach
	private void clearDB() {
		driver.session().run("MATCH (n) DETACH DELETE n");
		logger.info("Database cleared.");
	}

	@AfterAll
	public static void closeDBConnection() throws Exception {
		builder.close();
		logger.info("Database Connection closed.");
	}

	@Test
	public void testOneSokoban() {
		// Get an EMSL pattern
		Pattern p = (Pattern) patterns.getEntities().get(0);

		// Create a pattern and pass EMSL pattern
		IPattern ip = new NeoPattern(p, builder);

		// Ask for all matches
		var matches = ip.getMatches();

		// Check expected count
		assertThat(matches.size(), is(1));
	}

	@Test
	public void testOneSokobanStillValid() {
		Pattern p = (Pattern) patterns.getEntities().get(0);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		var matchesCount = 0;

		for (IMatch m : matches) {
			if (m.isStillValid())
				matchesCount++;
		}
		assertThat(matchesCount, is(matches.size()));
	}

	@Test
	public void testOneBlock() {

		Pattern p = (Pattern) patterns.getEntities().get(1);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		assertThat(matches.size(), is(2));
	}

	@Test
	public void testOneBlockStillValid() {

		Pattern p = (Pattern) patterns.getEntities().get(1);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		var matchesCount = 0;

		for (IMatch m : matches) {
			if (m.isStillValid())
				matchesCount++;
		}
		assertThat(matchesCount, is(matches.size()));
	}

	@Test
	public void testOneEndField() {

		Pattern p = (Pattern) patterns.getEntities().get(2);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		assertThat(matches.size(), is(2));
	}

	@Test
	public void testOneEndFieldStillValid() {

		Pattern p = (Pattern) patterns.getEntities().get(2);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		var matchesCount = 0;

		for (IMatch m : matches) {
			if (m.isStillValid())
				matchesCount++;
		}
		assertThat(matchesCount, is(matches.size()));
	}

	@Test
	public void testOccupiedFields() {

		Pattern p = (Pattern) patterns.getEntities().get(3);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		assertThat(matches.size(), is(9));
	}

	@Test
	public void testOccupiedFieldsStillValid() {

		Pattern p = (Pattern) patterns.getEntities().get(3);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		var matchesCount = 0;

		for (IMatch m : matches) {
			if (m.isStillValid())
				matchesCount++;
		}
		assertThat(matchesCount, is(matches.size()));
	}

	@Test
	public void testOccupiedFieldsStillValidDeletedBlocks() {

		Pattern p = (Pattern) patterns.getEntities().get(3);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();

		// removing 2 blocks, valid matches should be 2 less
		driver.session().run("MATCH (b:Block) DETACH DELETE b");

		var matchesCount = 0;

		for (IMatch m : matches) {
			if (m.isStillValid())
				matchesCount++;
		}
		assertThat(matchesCount, is(matches.size() - 2));
	}

	@Test
	public void testOccupiedSokobanFields() {

		Pattern p = (Pattern) patterns.getEntities().get(4);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		assertThat(matches.size(), is(1));
	}

	@Test
	public void testOccupiedSokobanFieldsStillValid() {

		Pattern p = (Pattern) patterns.getEntities().get(4);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		var matchesCount = 0;

		for (IMatch m : matches) {
			if (m.isStillValid())
				matchesCount++;
		}
		assertThat(matchesCount, is(matches.size()));
	}

	@Test
	public void testOccupiedBlockFields() {

		Pattern p = (Pattern) patterns.getEntities().get(5);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		assertThat(matches.size(), is(2));
	}

	@Test
	public void testOccupiedBlockFieldsStillValid() {

		Pattern p = (Pattern) patterns.getEntities().get(5);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		var matchesCount = 0;

		for (IMatch m : matches) {
			if (m.isStillValid())
				matchesCount++;
		}
		assertThat(matchesCount, is(matches.size()));
	}

	@Test
	public void testOccupiedBoulderFields() {

		Pattern p = (Pattern) patterns.getEntities().get(6);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		assertThat(matches.size(), is(8));
	}

	@Test
	public void testOccupiedBoulderFieldsStillValid() {

		Pattern p = (Pattern) patterns.getEntities().get(6);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		var matchesCount = 0;

		for (IMatch m : matches) {
			if (m.isStillValid())
				matchesCount++;
		}
		assertThat(matchesCount, is(matches.size()));
	}

	@Test
	public void testAllFieldsInARow() {

		Pattern p = (Pattern) patterns.getEntities().get(7);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		assertThat(matches.size(), is(4));
	}

	@Test
	public void testAllNotBorderFieldsInARow() {
		Pattern p = (Pattern) patterns.getEntities().get(8);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		assertThat(matches.size(), is(2));
	}

	@Test
	public void testAllNotBorderFieldsInARowStillValidDeletedEdges() {

		Pattern p = (Pattern) patterns.getEntities().get(8);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();

		// removing all right edges
		driver.session().run("MATCH (f:Field)-[r:right]->(g:Field) DETACH DELETE r");

		var matchesCount = 0;

		for (IMatch m : matches) {
			if (m.isStillValid())
				matchesCount++;
		}
		assertThat(matchesCount, is(0));
	}

	@Test
	public void testAllNotBorderFieldsInARowAndCol() {
		Pattern p = (Pattern) patterns.getEntities().get(9);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		assertThat(matches.size(), is(1));
	}

	@Test
	public void testAllNotBorderFieldsInDiffRows() {
		Pattern p = (Pattern) patterns.getEntities().get(10);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		assertThat(matches.size(), is(0));
	}

	@Test
	public void testAll3x3Fields() {
		Pattern p = (Pattern) patterns.getEntities().get(11);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		assertThat(matches.size(), is(4));
	}

	@Test
	public void testAll3x3FieldsIsStillValid() {
		Pattern p = (Pattern) patterns.getEntities().get(11);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		var matchesCount = 4;

		for (IMatch m : matches) {
			if (m.isStillValid())
				matchesCount++;
		}
		assertThat(matchesCount, is(matches.size()));
	}

}
