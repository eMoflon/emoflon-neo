package org.emoflon.neo.example.sokoban.patterns;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Scanner;

import org.apache.log4j.Logger;
import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.API_Models_SokobanSimpleTestField;
import org.emoflon.neo.api.API_Rules_SokobanPatternsRulesConstraints;
import org.emoflon.neo.emsl.eMSL.Model;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.example.sokoban.scalability.ScalabilityTest;
import org.emoflon.neo.neo4j.adapter.NeoCoreBuilder;
import org.emoflon.neo.neo4j.adapter.NeoPattern;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.StatementResult;

public class PatternTest {

	private static Scanner reader;
	private static final Logger logger = Logger.getLogger(ScalabilityTest.class);
	private static NeoCoreBuilder builder = API_Common.createBuilder();
	private static Driver driver = builder.getDriver();
	
	private API_Rules_SokobanPatternsRulesConstraints rules = new API_Rules_SokobanPatternsRulesConstraints(builder);
	private Model model = (new API_Models_SokobanSimpleTestField(builder)).getModel_SokobanSimpleTestField();

	@BeforeAll
	private static void startDBConnection() throws Exception {
		logger.info("Database Connection established.");
		StatementResult result = driver.session().run("MATCH (n) RETURN count(n)");
		
		//create Index on Matching Nodes
		
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
		
		//driver.session().run("DROP CONSTRAINT ON (m:Match) ASSERT m.uuid IS UNIQUE");
		//driver.session().run("CREATE INDEX ON :Match(uuid)");
		//driver.session().run("DROP INDEX ON :Match(uuid)");
		//driver.session().run("CREATE CONSTRAINT ON (m:Match) ASSERT m.uuid IS UNIQUE");

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
	public void test_OneSokoban() {
		NeoPattern p = rules.getPattern_OneSokoban();
		var matches = p.getMatches();
		assertThat(matches.size(), is(1));
	}
	
	@Test
	public void test_TwoSokoban() {
		NeoPattern p = rules.getPattern_TwoSokoban();
		var matches = p.getMatches();
		assertThat(matches.size(), is(0));
	}

	@Test
	public void test_OneSokoban_StillValid() {
		NeoPattern p = rules.getPattern_OneSokoban();
		var matches = p.getMatches();
		var matchesCount = 0;

		for (IMatch m : matches) {
			if (m.isStillValid())
				matchesCount++;
		}
		assertThat(matchesCount, is(matches.size()));
	}

	@Test
	public void test_OneBlock() {
		NeoPattern p = rules.getPattern_OneBlock();
		var matches = p.getMatches();
		assertThat(matches.size(), is(2));
	}

	@Test
	public void test_OneBlock_StillValid() {
		NeoPattern p = rules.getPattern_OneBlock();
		var matches = p.getMatches();
		var matchesCount = 0;

		for (IMatch m : matches) {
			if (m.isStillValid())
				matchesCount++;
		}
		assertThat(matchesCount, is(matches.size()));
	}

	@Test
	public void test_OneEndField() {
		NeoPattern p = rules.getPattern_OneEndField();
		var matches = p.getMatches();
		assertThat(matches.size(), is(2));
	}

	@Test
	public void test_OneEndField_StillValid() {
		NeoPattern p = rules.getPattern_OneEndField();
		var matches = p.getMatches();
		var matchesCount = 0;

		for (IMatch m : matches) {
			if (m.isStillValid())
				matchesCount++;
		}
		assertThat(matchesCount, is(matches.size()));
	}

	@Test
	public void test_OccupiedField() {
		NeoPattern p = rules.getPattern_OccupiedField();
		var matches = p.getMatches();
		assertThat(matches.size(), is(9));
	}

	@Test
	public void test_OccupiedField_StillValid() {
		NeoPattern p = rules.getPattern_OccupiedField();
		var matches = p.getMatches();
		var matchesCount = 0;

		for (IMatch m : matches) {
			if (m.isStillValid())
				matchesCount++;
		}
		assertThat(matchesCount, is(matches.size()));
	}

	@Test
	public void test_OccupiedField_StillValid_AfterDeletingBlocks() {
		NeoPattern p = rules.getPattern_OccupiedField();
		var matches = p.getMatches();
		var matchesCount = 0;

		// removing 2 blocks, valid matches should be 2 less
		driver.session().run("MATCH (b:Block) DETACH DELETE b");

		for (IMatch m : matches) {
			if (m.isStillValid())
				matchesCount++;
		}
		assertThat(matchesCount, is(matches.size() - 2));
	}

	@Test
	public void test_AnOccupiedSokobanField() {
		NeoPattern p = rules.getPattern_AnOccupiedSokobanField();
		var matches = p.getMatches();
		assertThat(matches.size(), is(1));
	}

	@Test
	public void test_AnOccupiedSokobanField_StillValid() {
		NeoPattern p = rules.getPattern_AnOccupiedSokobanField();
		var matches = p.getMatches();
		var matchesCount = 0;

		for (IMatch m : matches) {
			if (m.isStillValid())
				matchesCount++;
		}
		assertThat(matchesCount, is(matches.size()));
	}

	@Test
	public void test_AnOccupiedBlockField() {
		NeoPattern p = rules.getPattern_AnOccupiedBlockField();
		var matches = p.getMatches();
		assertThat(matches.size(), is(2));
	}

	@Test
	public void test_AnOccupiedBlockField_StillValid() {
		NeoPattern p = rules.getPattern_AnOccupiedBlockField();
		var matches = p.getMatches();
		var matchesCount = 0;

		for (IMatch m : matches) {
			if (m.isStillValid())
				matchesCount++;
		}
		assertThat(matchesCount, is(matches.size()));
	}

	@Test
	public void test_AnOccupiedBoulderField() {
		NeoPattern p = rules.getPattern_AnOccupiedBoulderField();
		var matches = p.getMatches();
		assertThat(matches.size(), is(8));
	}

	@Test
	public void test_AnOccupiedBoulderField_StillValid() {
		NeoPattern p = rules.getPattern_AnOccupiedBoulderField();
		var matches = p.getMatches();
		var matchesCount = 0;

		for (IMatch m : matches) {
			if (m.isStillValid())
				matchesCount++;
		}
		assertThat(matchesCount, is(matches.size()));
	}

	@Test
	public void test_AllFieldsInARow() {
		NeoPattern p = rules.getPattern_AllFieldsInARow();
		var matches = p.getMatches();
		assertThat(matches.size(), is(4));
	}

	@Test
	public void test_AllNotBorderFieldsInARow() {
		NeoPattern p = rules.getPattern_AllNotBorderFieldsInARow();
		var matches = p.getMatches();
		assertThat(matches.size(), is(2));
	}

	@Test
	public void test_AllNotBorderFieldsInARow_StillValid_AfterDeletingEdges() {
		NeoPattern p = rules.getPattern_AllFieldsInARow();
		var matches = p.getMatches();
		var matchesCount = 0;

		// removing all right edges
		driver.session().run("MATCH (f:Field)-[r:right]->(g:Field) DETACH DELETE r");

		for (IMatch m : matches) {
			if (m.isStillValid())
				matchesCount++;
		}
		assertThat(matchesCount, is(4));
	}

	@Test
	public void test_AllNotBorderFieldsInARowAndCol() {
		NeoPattern p = rules.getPattern_AllNotBorderFieldsInARowAndCol();
		var matches = p.getMatches();
		assertThat(matches.size(), is(1));
	}

	@Test
	public void test_AllNotBorderFieldsInDiffRows() {
		NeoPattern p = rules.getPattern_AllNotBorderFieldsInDiffRows();
		var matches = p.getMatches();
		assertThat(matches.size(), is(0));
	}

	@Test
	public void test_All3x3Fields() {
		NeoPattern p = rules.getPattern_All3x3Fields();
		var matches = p.getMatches();
		assertThat(matches.size(), is(4));
	}

	@Test
	public void test_All3x3Fields_StillValid() {
		NeoPattern p = rules.getPattern_All3x3Fields();
		var matches = p.getMatches();
		var matchesCount = 0;

		for (IMatch m : matches) {
			if (m.isStillValid())
				matchesCount++;
		}
		assertThat(matchesCount, is(matches.size()));
	}
	
	@Test
	public void test_All3x3Fields_StillValid_AfterDeletingEdges() {
		NeoPattern p = rules.getPattern_All3x3Fields();
		var matches = p.getMatches();
		var matchesCount = 0;
		
		// removing all right and bottom edges of endPos fields
		driver.session().run("MATCH (f:Field {endPos: true, name: \"f32\"})-[r:right]->(g:Field) DETACH DELETE f");
		
		for (IMatch m : matches) {
			if (m.isStillValid()) {
				matchesCount++;
			}
		}
		assertThat(matchesCount, is(matches.size()-2));
	}
	
	// Only for test purposes
	@Test
	public void test_empty() {
		
		assertThat(0, is(0));
	}

}
