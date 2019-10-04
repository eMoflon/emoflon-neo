package org.emoflon.neo.example.sokoban;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.models.API_SokobanSimpleTestField;
import org.emoflon.neo.api.rules.API_SokobanPatternsRulesConstraints;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.example.ENeoTest;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;
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
	public void testMoveSokobanDownNewNodes() {
		IRule<NeoMatch, NeoCoMatch> rule = entities.getRule_ForTestOnlyMoveSokobanDownAndNewNodes().rule();
		var result = rule.apply();
		assertTrue(result.isPresent());
	}
	
	@Test
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
	
	@Test
	public void testMoveSokobanDownNewNodesWithCond() {
		IRule<NeoMatch, NeoCoMatch> rule = entities.getRule_ForTestOnlymoveSokobanDownWithConditionAndNewNodes().rule();
		var matches = rule.determineMatches();
		assertTrue(matches.size() == 1);
		var onlyMatch = matches.iterator().next();
		Optional<NeoCoMatch> result = rule.apply(onlyMatch);
		assertTrue(result.isPresent());
	}
	
	@Test 
	public void testMoveSokobanDownPathsWithCond() {
		IRule<NeoMatch, NeoCoMatch> rule = entities.getRule_MoveSokobanDownTest().rule();
		var matches = rule.determineMatches();
		assertTrue(matches.size() == 0);		
	}
	
	@Test 
	public void testAssignEndPosToNeighboringEndPosField() {
		IRule<NeoMatch, NeoCoMatch> rule = entities.getRule_TestAttributeAssignmentsWithElementsValueAssign().rule();
		var matches = rule.determineMatches();
		assertEquals(2, matches.size());	
		
		var iterator = matches.iterator();
		
		while(iterator.hasNext()) {
			var match = iterator.next();
			Optional<NeoCoMatch> result = rule.apply(match);
			assertTrue(result.isPresent());
			assertFalse(match.isStillValid());
		}
	}
	
	@Test 
	public void testMoveSokobanRightWhenForbidOccupiedField() {
		IRule<NeoMatch, NeoCoMatch> rule = entities.getRule_MoveSokobanRightWhenForbidOccupiedField().rule();
		var matches = rule.determineMatches();
		assertEquals(1, matches.size());	
		
		var iterator = matches.iterator();
		
		while(iterator.hasNext()) {
			var match = iterator.next();
			Optional<NeoCoMatch> result = rule.apply(match);
			assertTrue(result.isPresent());
			assertFalse(match.isStillValid());
		}
	}
	
	@Test 
	public void testChangeToEndField() {
		IRule<NeoMatch, NeoCoMatch> rule = entities.getRule_ChangeToEndField().rule();
		var matches = rule.determineMatches();
		assertEquals(10, matches.size());	
		
		var iterator = matches.iterator();
		
		while(iterator.hasNext()) {
			var match = iterator.next();
			Optional<NeoCoMatch> result = rule.apply(match);
			assertTrue(result.isPresent());
			assertFalse(match.isStillValid());
		}
	}
	
	@Test
	public void testMoveBlockUp() {
		IRule<NeoMatch, NeoCoMatch> rule = entities.getRule_MoveBlockUp().rule();
		var matches = rule.determineMatches();
		assertEquals(2, matches.size());
		
		var tempMatches = rule.isStillApplicable(matches);
		var validMatches = new ArrayList<NeoMatch>(matches);
		for(var match : matches) {
			if(tempMatches.containsKey(match.getHashCode()) && !tempMatches.get(match.getHashCode())) {
				validMatches.remove(match);
			}
		}
		matches = validMatches;
		assertEquals(2, matches.size());
		
		Optional<Collection<NeoCoMatch>> result = rule.applyAll(matches);
		assertTrue(result.isPresent());
		
		var coMatches = new ArrayList<NeoMatch>();
		for(var co : result.get()) {
			coMatches.add((NeoMatch)co);
		}
		assertEquals(2,coMatches.size());
		
		tempMatches = rule.isStillApplicable(matches);
		validMatches = new ArrayList<NeoMatch>(matches);
		for(var match : matches) {
			if(tempMatches.containsKey(match.getHashCode()) && !tempMatches.get(match.getHashCode())) {
				validMatches.remove(match);
			}
		}
		matches = validMatches;
		assertEquals(0, matches.size());
		
	}
	
	@Test
	public void testRemoveSokoobanForDangelingEdgesSPO() {
		IRule<NeoMatch, NeoCoMatch> rule = entities.getRule_RemoveSokoban().rule();
		var matches = rule.determineMatches();
		assertEquals(1, matches.size());
		
		rule.useSPOSemantics(true);
		
		var match = matches.iterator().next();
		
		var comatches = rule.apply(match);
		assertTrue(comatches.isPresent());
		
	}
	
	// FIXME
	@Test
	public void testRemoveSokoobanForDangelingEdgesDPO() {
		IRule<NeoMatch, NeoCoMatch> rule = entities.getRule_RemoveSokoban().rule();
		var matches = rule.determineMatches();
		assertEquals(1, matches.size());
		
		rule.useSPOSemantics(false);
		
		var match = matches.iterator().next();
		
		try {
			rule.apply(match);
			assertTrue(false);
		} catch (Exception e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void testRemoveSokoobanWithDangelingEdgesSPO() {
		IRule<NeoMatch, NeoCoMatch> rule = entities.getRule_RemoveSokobanWithDanglingEdges().rule();
		var matches = rule.determineMatches();
		assertEquals(1, matches.size());
		
		rule.useSPOSemantics(true);
		
		var match = matches.iterator().next();
		
		var comatches = rule.apply(match);
		assertTrue(comatches.isPresent());
		
	}
	
	@Test
	public void testRemoveSokoobanWithDangelingEdgesDPO() {
		IRule<NeoMatch, NeoCoMatch> rule = entities.getRule_RemoveSokobanWithDanglingEdges().rule();
		var matches = rule.determineMatches();
		assertEquals(1, matches.size());
		
		rule.useSPOSemantics(false);
		
		var match = matches.iterator().next();
		
		var comatches = rule.apply(match);
		assertTrue(comatches.isPresent());
		
	}
}
