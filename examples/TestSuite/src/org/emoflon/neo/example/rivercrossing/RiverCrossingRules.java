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
	public void test_SolveCompleteGame() {
		
		// (F W C G)  <-----> ()
		
		// Move Goat to other side
		IRule<NeoMatch, NeoCoMatch> moveGoat = entities.getRule_MoveGoatToOtherSide().rule();
		var matchesMoveGoat = moveGoat.determineMatches();
		assertEquals(1, matchesMoveGoat.size());
		var matchMoveGoat = matchesMoveGoat.iterator().next();
		moveGoat.apply(matchMoveGoat);
		assertFalse(matchMoveGoat.isStillValid());
		
		// (W C)  <-----> (F G)
		assertTrue(entities.getConstraint_ForbidOneEatsAnother().isSatisfied());
		
		// Move empty back
		IRule<NeoMatch, NeoCoMatch> moveEmpty = entities.getRule_MoveEmptyToOtherSide().rule();
		var matchesMoveEmpty = moveEmpty.determineMatches();
		assertEquals(1, matchesMoveEmpty.size());
		var matchMoveEmpty = matchesMoveEmpty.iterator().next();
		moveEmpty.apply(matchMoveEmpty);
		assertFalse(matchMoveEmpty.isStillValid());
		
		// (F W C)  <-----> (G)
		assertTrue(entities.getConstraint_ForbidOneEatsAnother().isSatisfied());
		
		// Move Wolf to other side
		IRule<NeoMatch, NeoCoMatch> moveWolf = entities.getRule_MoveWolfToOtherSide().rule();
		var matchesMoveWolf = moveWolf.determineMatches();
		assertEquals(1, matchesMoveWolf.size());
		var matchMoveWolf = matchesMoveWolf.iterator().next();
		moveWolf.apply(matchMoveWolf);
		assertFalse(matchMoveWolf.isStillValid());
		
		// (C)  <-----> (F W G)
		assertTrue(entities.getConstraint_ForbidOneEatsAnother().isSatisfied());
		
	}
	
}
