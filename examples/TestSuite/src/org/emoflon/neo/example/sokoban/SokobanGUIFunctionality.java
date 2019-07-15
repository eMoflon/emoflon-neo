package org.emoflon.neo.example.sokoban;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;

import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.models.API_SokobanSimpleTestField;
import org.emoflon.neo.api.org.moflon.tutorial.sokobangamegui.patterns.API_SokobanGUIPatterns;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.moflon.tutorial.sokobangamegui.controller.IController;

public class SokobanGUIFunctionality extends ENeoTest {
	private API_SokobanGUIPatterns entities = new API_SokobanGUIPatterns(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI);

	@BeforeEach
	public void initDB() {
		initDB(new API_SokobanSimpleTestField(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI).getModel_SokobanSimpleTestField());
	}
	
	@Test
	public void testExactlyOneSokoban() {
		assertTrue(entities.getConstraint_ExactlyOneSokoban().isSatisfied());
	}
	
	@Test
	@Disabled("//TODO[Jannik] Test for path expression")
	public void testFigureTypes() {
		var access = entities.getPattern_FigureTypes();
		var types = access.matcher().determineMatches().stream()//
			.map(m -> access.data(m).eclass.ename)
			.collect(Collectors.toList());
		
		assertEquals(3, types.size());
		
		assertTrue(types.contains(IController.BLOCK));
		assertTrue(types.contains(IController.SOKOBAN));
		assertTrue(types.contains(IController.BOULDER));
	}
}
