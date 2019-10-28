package org.emoflon.neo.example.rivercrossing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.API_RiverCrossing;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.example.ENeoTest;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RiverCrossingRules extends ENeoTest {
	private API_RiverCrossing entities = new API_RiverCrossing(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI);
	
	@BeforeEach
	public void initDB() {
		initDB(entities.getModel_RiverCrossingStart());
	}
	
	@Test
	public void test_completeGame() {
		
		// (F W C G)  <-----> ()
		assertFalse(entities.getConstraint_GameEnded().isSatisfied());
		
		// Move Goat to other side
		IRule<NeoMatch, NeoCoMatch> moveGoat = entities.getRule_MoveGoatToOtherSide().rule();
		var matchesMoveGoat = moveGoat.determineMatches();
		assertEquals(1, matchesMoveGoat.size());
		var matchMoveGoat = matchesMoveGoat.iterator().next();
		moveGoat.apply(matchMoveGoat);
		expectInvalidMatch(matchMoveGoat);
		
		// (W C)  <-----> (F G)
		assertTrue(entities.getConstraint_ForbidOneEatsAnother().isSatisfied());
		assertFalse(entities.getConstraint_GameEnded().isSatisfied());
		
		// Move empty back
		IRule<NeoMatch, NeoCoMatch> moveEmpty = entities.getRule_MoveEmptyToOtherSide().rule();
		var matchesMoveEmpty = moveEmpty.determineMatches();
		assertEquals(1, matchesMoveEmpty.size());
		var matchMoveEmpty = matchesMoveEmpty.iterator().next();
		moveEmpty.apply(matchMoveEmpty);
		expectInvalidMatch(matchMoveEmpty);
		
		// (F W C)  <-----> (G)
		assertTrue(entities.getConstraint_ForbidOneEatsAnother().isSatisfied());
		assertFalse(entities.getConstraint_GameEnded().isSatisfied());
		
		// Move Wolf to other side
		IRule<NeoMatch, NeoCoMatch> moveWolf = entities.getRule_MoveWolfToOtherSide().rule();
		var matchesMoveWolf = moveWolf.determineMatches();
		assertEquals(1, matchesMoveWolf.size());
		var matchMoveWolf = matchesMoveWolf.iterator().next();
		moveWolf.apply(matchMoveWolf);
		expectInvalidMatch(matchMoveWolf);
		
		// (C)  <-----> (F W G)
		assertTrue(entities.getConstraint_ForbidOneEatsAnother().isSatisfied());
		assertFalse(entities.getConstraint_GameEnded().isSatisfied());
		
		// Move Goat to other side
		matchesMoveGoat = moveGoat.determineMatches();
		assertEquals(1, matchesMoveGoat.size());
		matchMoveGoat = matchesMoveGoat.iterator().next();
		moveGoat.apply(matchMoveGoat);
		expectInvalidMatch(matchMoveGoat);
		
		// (F C G)  <-----> (W)
		assertTrue(entities.getConstraint_ForbidOneEatsAnother().isSatisfied());
		assertFalse(entities.getConstraint_GameEnded().isSatisfied());
		
		// Move Cabbage to other side
		IRule<NeoMatch, NeoCoMatch> moveCabbage = entities.getRule_MoveCabbageToOtherSide().rule();
		var matchesMoveCabbage = moveCabbage.determineMatches();
		assertEquals(1, matchesMoveCabbage.size());
		var matchMoveCabbage = matchesMoveCabbage.iterator().next();
		moveCabbage.apply(matchMoveCabbage);
		expectInvalidMatch(matchMoveCabbage);
		
		// (G)  <-----> (F W C)
		assertTrue(entities.getConstraint_ForbidOneEatsAnother().isSatisfied());
		assertFalse(entities.getConstraint_GameEnded().isSatisfied());
		
		// Move empty back
		matchesMoveEmpty = moveEmpty.determineMatches();
		assertEquals(1, matchesMoveEmpty.size());
		matchMoveEmpty = matchesMoveEmpty.iterator().next();
		moveEmpty.apply(matchMoveEmpty);
		expectInvalidMatch(matchMoveEmpty);
		
		// (F G)  <-----> (W C)
		assertTrue(entities.getConstraint_ForbidOneEatsAnother().isSatisfied());
		assertFalse(entities.getConstraint_GameEnded().isSatisfied());
		
		// Move Goat to other side
		matchesMoveGoat = moveGoat.determineMatches();
		assertEquals(1, matchesMoveGoat.size());
		matchMoveGoat = matchesMoveGoat.iterator().next();
		moveGoat.apply(matchMoveGoat);
		expectInvalidMatch(matchMoveGoat);
		
		// ()  <-----> (F W C G)
		assertTrue(entities.getConstraint_ForbidOneEatsAnother().isSatisfied());
		assertTrue(entities.getConstraint_GameEnded().isSatisfied());
		
		// game finished		
	}
}
