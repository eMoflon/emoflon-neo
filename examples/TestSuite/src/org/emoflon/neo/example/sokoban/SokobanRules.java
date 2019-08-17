package org.emoflon.neo.example.sokoban;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.models.API_SokobanSimpleTestField;
import org.emoflon.neo.api.rules.API_SokobanPatternsRulesConstraints;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.example.ENeoTest;
import org.emoflon.neo.neo4j.adapter.NeoCoMatch;
import org.emoflon.neo.neo4j.adapter.NeoMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SokobanRules extends ENeoTest {
	private API_SokobanPatternsRulesConstraints entities = new API_SokobanPatternsRulesConstraints(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI);

	@BeforeEach
	public void initDB() {
		initDB(new API_SokobanSimpleTestField(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI).getModel_SokobanSimpleTestField());
	}
	
	@Test
	public void testMoveSokobanDown() {
		IRule<NeoMatch, NeoCoMatch> rule = entities.getRule_MoveSokobanDown().rule();
		var matches = rule.determineMatches();
		assertTrue(matches.size() == 1);
		
		var onlyMatch = matches.iterator().next();
		assertTrue(onlyMatch.isStillValid());
		
		Optional<NeoCoMatch> result = rule.apply(onlyMatch);
		assertTrue(result.isPresent());
		
		assertFalse(onlyMatch.isStillValid());
		
		Optional<NeoCoMatch> notPossible = rule.apply(onlyMatch);
		assertFalse(notPossible.isPresent());
	}
	
	@Test
	//@Disabled("TODO[Jannik] Implement rules")
	public void testMoveSokobanDownWithCond() {
		IRule<NeoMatch, NeoCoMatch> rule = entities.getRule_MoveSokobanDownWithCondition().rule();
		var matches = rule.determineMatches();
		assertTrue(matches.size() == 1);
		
		var onlyMatch = matches.iterator().next();
		assertTrue(onlyMatch.isStillValid());
		
		Optional<NeoCoMatch> result = rule.apply(onlyMatch);
		assertTrue(result.isPresent());
		
		assertFalse(onlyMatch.isStillValid());
		
		Optional<NeoCoMatch> notPossible = rule.apply(onlyMatch);
		assertFalse(notPossible.isPresent());
	}
}
