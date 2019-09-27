package org.emoflon.neo.example.pac_man;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.API_PacMan;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PacManRules extends ENeoTest {
	private API_PacMan entities = new API_PacMan(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI);
	
	@BeforeEach
	public void initDB() {
		initDB(entities.getModel_SimpleGameWithMarbles());
	}
	
	@Test
	public void test_CollectMarbles() {
		var access = entities.getRule_CollectMarble();
		assertEquals(1, access.rule().countMatches());
		var result = access.rule().apply();
		assertTrue(result.isPresent());
		var data = access.codata(result.get());
		assertEquals(1, data.pacMan.marbles);
	}
}