package org.emoflon.neo.example.fruitgarden;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.emoflon.neo.api.org.eneo.fruitgarden.API_FruitGardenLanguage;
import org.emoflon.neo.api.org.eneo.fruitgarden.API_PatternsAndRules;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FruitGardenTests extends ENeoTest {
	private API_FruitGardenLanguage language = new API_FruitGardenLanguage(builder);
	private API_PatternsAndRules entities = new API_PatternsAndRules(builder);
	
	@BeforeEach
	public void initDB() {
		initDB(language.getModel_SampleGarden());
	}
	
	@Test
	public void testGameOver() {
		assertTrue(entities.getConstraint_GameOver().isViolated());
		assertTrue(entities.getConstraint_GameIsWon().isViolated());
		assertTrue(entities.getConstraint_GameIsLost().isViolated());
	}
	
	@Test
	public void testMatchesForPatterns() {
		assertEquals(4, entities.getPattern_OneFruitOnTree().pattern().countMatches());
	}
	
	@Test
	public void testMatchesForRules() {
		assertEquals(1, entities.getRule_MoveCrowForward().rule().countMatches());
		assertEquals(1, entities.getRule_PickALemon().rule().countMatches());
		assertEquals(1, entities.getRule_PickAPlum().rule().countMatches());
		assertEquals(1, entities.getRule_PickAPear().rule().countMatches());
		assertEquals(1, entities.getRule_PickAnApple().rule().countMatches());
	}
	
	@Test
	public void testMoveCrow() {
		assertTrue(entities.getRule_MoveCrowForward().rule().apply().isPresent());
		assertTrue(entities.getRule_MoveCrowForward().rule().apply().isPresent());
		assertTrue(entities.getRule_MoveCrowForward().rule().apply().isPresent());
		assertFalse(entities.getRule_MoveCrowForward().rule().apply().isPresent());
		
		assertTrue(entities.getConstraint_GameOver().isSatisfied());
		assertTrue(entities.getConstraint_GameIsLost().isSatisfied());
	}
	
	@Test
	public void testPickFruit() {
		assertTrue(entities.getRule_PickALemon().rule().apply().isPresent());
		assertTrue(entities.getRule_PickAnApple().rule().apply().isPresent());
		assertTrue(entities.getRule_PickAPear().rule().apply().isPresent());
		assertTrue(entities.getRule_PickAPlum().rule().apply().isPresent());
		
		assertTrue(entities.getConstraint_GameIsWon().isSatisfied());
		assertTrue(entities.getConstraint_GameIsLost().isViolated());
	}
	
	@Test
	public void testPickFruitAndBack() {
		assertTrue(entities.getRule_PickALemon().rule().apply().isPresent());
		assertTrue(entities.getRule_PickALemonBack().rule().apply().isPresent());
	}
}
