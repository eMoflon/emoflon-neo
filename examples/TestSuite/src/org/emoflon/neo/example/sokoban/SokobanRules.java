package org.emoflon.neo.example.sokoban;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.emoflon.neo.api.models.API_SokobanSimpleTestField;
import org.emoflon.neo.api.rules.API_SokobanPatternsRulesConstraints;
import org.emoflon.neo.engine.api.rules.ICoMatch;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class SokobanRules extends ENeoTest {
	private API_SokobanPatternsRulesConstraints entities = new API_SokobanPatternsRulesConstraints(builder);

	@BeforeEach
	public void initDB() {
		initDB(new API_SokobanSimpleTestField(builder).getModel_SokobanSimpleTestField());
	}
	
	@Test
	@Disabled("TODO[Jannik] Implement rules")
	public void testMoveSokobanDown() {
		IRule rule = entities.getRule_MoveSokobanDown();
		var matches = rule.determineMatches();
		assertTrue(matches.size() == 1);
		var onlyMatch = matches.iterator().next();
		rule.apply(onlyMatch);
		assertFalse(onlyMatch.isStillValid());
		
		Optional<ICoMatch> result = rule.apply();
		assertTrue(result.isPresent());
		
		Optional<ICoMatch> notPossible = rule.apply();
		assertFalse(notPossible.isPresent());
	}
}
