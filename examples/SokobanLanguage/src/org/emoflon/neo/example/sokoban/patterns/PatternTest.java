package org.emoflon.neo.example.sokoban.patterns;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.EMSL_Spec;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.util.EMSUtil;
import org.emoflon.neo.engine.api.rules.IPattern;
import org.emoflon.neo.example.sokoban.scalability.ScalabilityTest;
import org.emoflon.neo.neo4j.adapter.NeoCoreBuilder;
import org.emoflon.neo.neo4j.adapter.NeoPattern;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.StatementResult;

public class PatternTest {
	
	private static final Logger logger = Logger.getLogger(ScalabilityTest.class);
	private static NeoCoreBuilder builder = new NeoCoreBuilder("bolt://localhost:11002", "neo4j", "test");
	
	private EMSL_Spec model = EMSUtil.loadSpecification(//
			"platform:/resource/SokobanLanguage/models/SokobanSimpleTestField.msl", //
			"../");

	private EMSL_Spec spec = EMSUtil.loadSpecification(//
			"platform:/resource/SokobanLanguage/rules/SokobanPatternsRulesConstraints.msl", //
			"../");
	
	private static Driver driver = builder.getDriver();
	
	@BeforeAll
	private static void startDBConnection() {
		logger.info("Database Connection established.");
		StatementResult result = driver.session().run("MATCH (n) RETURN count(n)");
		if(result.hasNext()) {
			if (result.next().get(0).asInt() > 0)
				logger.info("Database not empty. All data will be removed!");
			else
				logger.info("Database empty.");
		} else {
			logger.info("Database empty.");
		}
	}
	
	@BeforeEach
	private void initDB() {
		builder.exportEMSLEntityToNeo4j(model);
		logger.info("Database initialised.");
	}
	
	@AfterEach
	private void clearDB() {
		driver.session().run("MATCH (n) DETACH DELETE n");
		logger.info("Database cleared.");
	}
	
	@AfterAll
	public static void closeDBConnection() throws Exception{
		driver.session().close();
		driver.close();
		builder.close();
		logger.info("Database Connection closed.");
	}

	@Test
	public void testOneSokoban() {
		
		// Get an EMSL pattern
		Pattern p = (Pattern) spec.getEntities().get(0);

		// Create a pattern and pass EMSL pattern
		IPattern ip = new NeoPattern(p, builder);

		// Ask for all matches
		var matches = ip.getMatches();

		// Check expected count
		assertThat(matches.size(), is(1));
	}
	
	@Test
	public void testOneBlock() {

		Pattern p = (Pattern) spec.getEntities().get(1);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		assertThat(matches.size(), is(2));
	}
	
	@Test
	public void testOneEndField() {

		Pattern p = (Pattern) spec.getEntities().get(2);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		assertThat(matches.size(), is(2));
	}
	
	@Test
	public void testOccupiedFields() {

		Pattern p = (Pattern) spec.getEntities().get(3);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		assertThat(matches.size(), is(9));
	}
	
	@Test
	public void testOccupiedSokobanFields() {

		Pattern p = (Pattern) spec.getEntities().get(4);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		assertThat(matches.size(), is(1));
	}
	@Test
	public void testOccupiedBlockFields() {

		Pattern p = (Pattern) spec.getEntities().get(5);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		assertThat(matches.size(), is(2));
	}
	@Test
	public void testOccupiedBoulderFields() {

		Pattern p = (Pattern) spec.getEntities().get(6);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		assertThat(matches.size(), is(8));
	}
	
	@Test
	public void testAllFieldsInARow() {

		Pattern p = (Pattern) spec.getEntities().get(7);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		assertThat(matches.size(), is(4));
	}
	
	@Test
	public void testAllNotBorderFieldsInARow() {
		Pattern p = (Pattern) spec.getEntities().get(8);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		assertThat(matches.size(), is(2));
	}
	
	@Test
	public void testAllNotBorderFieldsInARowAndCol() {
		Pattern p = (Pattern) spec.getEntities().get(9);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		assertThat(matches.size(), is(1));
	}
	
	@Test
	public void testAllNotBorderFieldsInDiffRows() {
		Pattern p = (Pattern) spec.getEntities().get(10);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		assertThat(matches.size(), is(0));
	}
	
}
