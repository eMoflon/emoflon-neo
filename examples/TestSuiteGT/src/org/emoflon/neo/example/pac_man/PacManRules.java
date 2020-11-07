package org.emoflon.neo.example.pac_man;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.emoflon.neo.api.pacman.API_PacMan;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PacManRules extends ENeoTest {
	private API_PacMan entities = new API_PacMan(builder);
	
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
		var data = access.codata(List.of(result.get()));
		assertEquals(1, data.findAny().get()._pacMan._marbles);
	}
}