package org.emoflon.neo.example.sokoban;

import static org.junit.Assert.assertFalse;
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
	private API_SokobanGUIPatterns entities = new API_SokobanGUIPatterns(builder, API_Common.PLATFORM_RESOURCE_URI,
			API_Common.PLATFORM_PLUGIN_URI);

	@BeforeEach
	public void initDB() {
		initDB(new API_SokobanSimpleTestField(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI)
				.getModel_SokobanSimpleTestField());
	}

	@Test
	public void testExactlyOneSokoban() {
		assertTrue(entities.getConstraint_ExactlyOneSokoban().isSatisfied());
	}

	@Test
	public void testFigureTypes() {
		var access = entities.getPattern_FigureTypes();
		var types = access.matcher().determineMatches().stream()//
				.map(m -> access.data(m).eclass.ename).collect(Collectors.toList());

		assertEquals(3, types.size());

		assertTrue(types.contains(IController.BLOCK));
		assertTrue(types.contains(IController.SOKOBAN));
		assertTrue(types.contains(IController.BOULDER));
	}

	@Test
	public void testCreateBlock() {
		var access = entities.getRule_CreateBlock();
		var mask = access.mask();
		mask.setB_fields_0_fCol(1);
		mask.setB_fields_0_fRow(1);
		var result = access.rule(mask).apply();
		assertTrue(result.isPresent());
		var fieldId = result.get().getIdForNode(access.f);

		var testAccess = entities.getPattern_Occupied();
		var testMask = testAccess.mask();
		testMask.setField(fieldId);
		assertEquals(1, testAccess.matcher(testMask).countMatches());

		assertEquals(1, access.data(result.get()).b_fields_0_f.row);
		assertEquals(1, access.data(result.get()).b_fields_0_f.col);
	}

	@Disabled("TODO[Jannik] Waiting for #169")
	@Test
	public void testAxiom() {
		builder.clearDataBase();

		var access = entities.getRule_CreateTopLeft();
		assertEquals(1, access.rule().countMatches(),
				"All elements are green so there should be exactly one empty match!");

		// Testing limit in this case too
		assertTrue(access.rule().determineOneMatch().isPresent());

		var result = access.rule().apply();
		assertTrue(result.isPresent());

		// Rule is still applicable
		result = access.rule().apply();
		assertTrue(result.isPresent());

		assertEquals(1, access.rule().countMatches());
		assertTrue(access.rule().determineOneMatch().isPresent());
	}

	@Disabled("TODO[Jannik] Waiting for #169")
	@Test
	public void testAxiomWithAppCond() {
		builder.clearDataBase();

		var access = entities.getRule_CreateTopLeftWithAppCond();
		assertEquals(1, access.rule().countMatches(),
				"All elements are green so there should be exactly one empty match!");

		// Testing limit in this case too
		assertTrue(access.rule().determineOneMatch().isPresent());

		var result = access.rule().apply();
		assertTrue(result.isPresent());

		// It shouldn't be possible to apply the rule as its application condition
		// should block it
		result = access.rule().apply();
		assertFalse(result.isPresent());

		// Rule is no longer applicable
		assertEquals(0, access.rule().countMatches());
		assertFalse(access.rule().determineOneMatch().isPresent());
	}
}
