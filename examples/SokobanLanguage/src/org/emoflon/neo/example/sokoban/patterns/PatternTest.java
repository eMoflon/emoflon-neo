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
	@Test
	public void testPattern() {
		NeoCoreBuilder builder = new NeoCoreBuilder("bolt://localhost:7687", "neo4j", "test");
		
		// Load EMSL spec
		EMSL_Spec spec = EMSUtil.loadSpecification(//
				"platform:/resource/SokobanLanguage/rules/SokobanPatternsRulesConstraints.msl", //
				"../");

		// Get an EMSL pattern
		Pattern p = (Pattern) spec.getEntities().get(0);

		// Create a pattern and pass EMSL pattern
		IPattern ip = new NeoPattern(p, builder);

		// Ask for all matches
		var matches = ip.getMatches();

		// Check expected count
		assertThat(matches.size(), is(1));
	}
}
