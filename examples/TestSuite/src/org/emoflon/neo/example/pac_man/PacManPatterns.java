package org.emoflon.neo.example.pac_man;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.API_PacMan;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PacManPatterns extends ENeoTest {
	private API_PacMan entities = new API_PacMan(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI);
	
	@BeforeEach
	public void initDB() {
		initDB(entities.getModel_SimplePacManGame());
	}
	
	@Test
	public void test_PacManOnField() {
		expectSingleMatch(entities.getPattern_PacManOnField());
	}
	
	@Test
	public void test_GhostOnField() {
		assertThat(entities.getPattern_GhostOnField().matcher().countMatches(), is(2));
	}
	
	@Test
	public void test_PacManAndGhostOnNeighbouringFields() {
		expectSingleMatch(entities.getPattern_PacManAndGhostOnNeighbouringFields());
	}
	
	@Test
	public void test_TwoPacManOnBoard() {
		expectNoMatch(entities.getPattern_TwoPacManOnBoard());
	}
}
