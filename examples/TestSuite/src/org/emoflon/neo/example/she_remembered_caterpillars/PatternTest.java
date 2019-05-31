package org.emoflon.neo.example.she_remembered_caterpillars;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.emoflon.neo.api.API_Emsl_SheRememberedCaterpillars;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class PatternTest extends ENeoTest {
	private API_Emsl_SheRememberedCaterpillars entities = new API_Emsl_SheRememberedCaterpillars(builder);
	
	@BeforeEach
	private void initDB() {
		initDB(entities.getModel_SimpleGame());
	}
	
	@Test
	@Disabled("TODO[Jannik] Extend patterns to handle attribute expressions")
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
		assertThat(entities.getPattern_ColouredThingOnPlatform().countMatches(), is(2));
	}
	
	@Test
	@Disabled("TODO[Jannik] Handle when conditions")
	public void testNoWayForward() {
		expectNoMatch(entities.getPattern_NoWayForward());
	}
	
	@Test
	public void testNoDeadEnd() {
		expectSingleMatch(entities.getPattern_NoDeadEnd());
	}
}
