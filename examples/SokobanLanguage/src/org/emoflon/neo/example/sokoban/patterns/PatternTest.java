package org.emoflon.neo.example.sokoban.patterns;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.emoflon.neo.emsl.eMSL.EMSL_Spec;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.util.EMSUtil;
import org.emoflon.neo.engine.api.rules.IPattern;
import org.emoflon.neo.neo4j.adapter.NeoCoreBuilder;
import org.emoflon.neo.neo4j.adapter.NeoPattern;
import org.junit.jupiter.api.Test;

public class PatternTest {
	
	NeoCoreBuilder builder = new NeoCoreBuilder("bolt://localhost:7687", "neo4j", "test");
	
	// Load EMSL spec
	EMSL_Spec spec = EMSUtil.loadSpecification(//
			"platform:/resource/SokobanLanguage/rules/SokobanPatternsRulesConstraints.msl", //
			"../");

	@Test
	public void testOneSokoban() {
		
		// Get an EMSL pattern
		Pattern p = (Pattern) spec.getEntities().get(1);

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
		assertThat(matches.size(), is(1));
	}
	
	@Test
	public void testOneEndField() {

		Pattern p = (Pattern) spec.getEntities().get(2);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		assertThat(matches.size(), is(1));
	}
	
	@Test
	public void testOccupiedFields() {

		Pattern p = (Pattern) spec.getEntities().get(3);
		IPattern ip = new NeoPattern(p, builder);
		var matches = ip.getMatches();
		assertThat(matches.size(), is(2));
	}
	
	@Test
	public void testBlocksIsEqualToEndFields() {

		Pattern p1 = (Pattern) spec.getEntities().get(3);
		IPattern ip1 = new NeoPattern(p1, builder);
		var matches1 = ip1.getMatches();
		Pattern p2 = (Pattern) spec.getEntities().get(3);
		IPattern ip2 = new NeoPattern(p2, builder);
		var matches2 = ip2.getMatches();
		
		assertThat(matches1 == matches2, is(true));
	}
	
}
