package org.emoflon.neo.example.sokoban.patterns;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.emoflon.neo.emsl.eMSL.EMSL_Spec;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.util.EMSUtil;
import org.emoflon.neo.engine.api.rules.IPattern;
import org.emoflon.neo.neo4j.adapter.NeoCoreBuilder;
import org.emoflon.neo.neo4j.adapter.NeoPattern;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.Driver;

public class PatternTest {
	
	NeoCoreBuilder builder = new NeoCoreBuilder("bolt://localhost:11002", "neo4j", "test");
	EMSL_Spec spec = EMSUtil.loadSpecification(//
			"platform:/resource/SokobanLanguage/rules/SokobanPatternsRulesConstraints.msl", //
			"../");
	
	Driver driver = builder.getDriver();
	
	@BeforeClass
	public void startDBConnection() {
		
	}
	
	@Before
	public void initDB() {
		
	}
	
	@After
	public void clearDB() {
		
	}
	
	@BeforeEach
	public void resetDBtoInit() {
		
	}
	
	@AfterClass 
	public void closeDBConnection(){
		
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
