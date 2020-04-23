package org.emoflon.neo.example.she_remembered_caterpillars;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.emoflon.neo.api.API_SheRememberedCaterpillars;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SheRememberedCaterpillarsPatterns extends ENeoTest {
	private API_SheRememberedCaterpillars entities = new API_SheRememberedCaterpillars(builder);
	
	@BeforeEach
	public void initDB() {
		initDB(entities.getModel_SimpleGame());
	}
	
	@Test
	public void testPatternWithAttributeExpression() {
		expectSingleMatch(entities.getPattern_CanCrossBridge());
	}
	
	@Test
	public void testPatternWithEnum() {
		expectSingleMatch(entities.getPattern_EverythingBlue());
	}
	
	@Test
	public void testNoStrangeBridge() {
		expectNoMatch(entities.getPattern_StrangeBridge());
	}
	
	@Test
	public void testInFrontOfBridge() {
		expectSingleMatch(entities.getPattern_StandingInFrontOfBridge());
	}
	
	@Test
	public void testColouredThingsOnPlatforms() {
		assertThat(entities.getPattern_ColouredThingOnPlatform().pattern().countMatches(), is(2));
	}
	
	@Test
	public void testNoWayForward() {
		expectNoMatch(entities.getPattern_NoWayForward());
	}
	
	@Test
	public void testNoDeadEnd() {
		expectSingleMatch(entities.getPattern_NoDeadEnd());
	}
	
	@Test
	public void testNoStrangeBridges() {
		assertFalse(entities.getConstraint_StrangeBridges().isSatisfied());
	}
	
	@Test
	public void testCanCrossBridgeSomewhere() {
		assertTrue(entities.getConstraint_CanCrossBridgeSomewhere().isSatisfied());
	}
	
	@Test
	public void testAlwaysOnPlatform() {
		assertFalse(entities.getConstraint_AlwaysOnPlatform().isSatisfied());
	}
	
	@Test
	public void testNothingBlue() {
		assertFalse(entities.getConstraint_NothingBlue().isSatisfied());
		assertTrue(entities.getConstraint_NothingBlue().isViolated());
	}
	
	@Test
	public void testRuleWithAttributeAssignment() {
		var attrAssg = entities.getRule_ColourBridgeRED();
		assertEquals(2, attrAssg.rule().countMatches());
		assertTrue(attrAssg.rule().apply().isPresent());
		assertEquals(0, attrAssg.rule().countMatches());
	}
	
}
