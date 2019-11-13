package org.emoflon.neo.example.sokoban;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.emoflon.neo.api.models.API_SokobanSimpleTestField;
import org.emoflon.neo.api.org.moflon.tutorial.sokobangamegui.patterns.API_SokobanGUIPatterns;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.moflon.tutorial.sokobangamegui.controller.IController;

public class SokobanGUIFunctionality extends ENeoTest {
	private API_SokobanGUIPatterns entities = new API_SokobanGUIPatterns(builder);

	@BeforeEach
	public void initDB() {
		initDB(new API_SokobanSimpleTestField(builder)
				.getModel_SokobanSimpleTestField());
	}

	@Test
	public void testExactlyOneSokoban() {
		assertTrue(entities.getConstraint_ExactlyOneSokoban().isSatisfied());
	}

	@Test
	public void testFigureTypes() {
		var access = entities.getPattern_FigureTypes();
		var matches = access.pattern().determineMatches();
		var data = access.data(matches);
		var types = data.map(m -> m.eclass.ename).collect(Collectors.toList());
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
		var result = access.apply(mask, access.mask());
		assertTrue(result.isPresent());
		var fieldId = result.get().getNodeIDFor(access.f);

		var testAccess = entities.getPattern_Occupied();
		var testMask = testAccess.mask();
		testMask.setField(fieldId);
		assertEquals(1, testAccess.countMatches(testMask));

		assertEquals(1, access.data(List.of(result.get())).findAny().get().b_fields_0_f.row);
		assertEquals(1, access.data(List.of(result.get())).findAny().get().b_fields_0_f.col);
	}

	@Test
	public void testAxiom() {
		builder.executeQueryForSideEffect("MATCH (f:SokobanLanguage__Field), (b:SokobanLanguage__Board), (m:SokobanLanguage__Figure) DETACH DELETE f,b,m");

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

	@Test
	public void testAxiomWithAppCond() {
		builder.executeQueryForSideEffect("MATCH (f:SokobanLanguage__Field), (b:SokobanLanguage__Board), (m:SokobanLanguage__Figure) DETACH DELETE f,b,m");

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
	
	@Test
	public void testEndFieldToggle() {
		{
			var access = entities.getRule_SetEndField();
			var mask = access.mask();
			mask.setB_fields_0_fCol(1);
			mask.setB_fields_0_fRow(1);
			var match = access.determineOneMatch(mask);

			assertTrue(match.isPresent());
			var m = match.get();
			assertFalse(access.data(List.of(m)).findAny().get().f.endPos);

			access.rule().apply(m);

			assertTrue(access.data(List.of(m)).findAny().get().f.endPos);
		}
		{
			var access = entities.getRule_SetNotEndField();
			var mask = access.mask();
			mask.setB_fields_0_fCol(1);
			mask.setB_fields_0_fRow(1);
			var match = access.determineOneMatch(mask);

			assertTrue(match.isPresent());
			var m = match.get();
			assertTrue(access.data(List.of(m)).findAny().get().f.endPos);

			access.rule().apply(m);

			assertFalse(access.data(List.of(m)).findAny().get().f.endPos);
		}
	}
}
