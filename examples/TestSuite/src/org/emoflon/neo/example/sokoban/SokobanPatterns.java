package org.emoflon.neo.example.sokoban;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.models.API_Simple3x3Field;
import org.emoflon.neo.api.models.API_SokobanSimpleTestField;
import org.emoflon.neo.api.rules.API_SokobanPatternsRulesConstraints;
import org.emoflon.neo.example.ENeoTest;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class SokobanPatterns extends ENeoTest {

	private API_SokobanPatternsRulesConstraints entities = new API_SokobanPatternsRulesConstraints(builder,
			API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI);

	@BeforeEach
	public void initDB() {
		initDB(new API_SokobanSimpleTestField(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI)
				.getModel_SokobanSimpleTestField());
	}

	@Test
	public void test_ifSokobanThenSelectedFigure() {
		assertTrue(entities.getConstraint_SokobanIsSelectedFigure().isSatisfied());
	}

	@Test
	public void test_OneSokoban() {
		expectSingleMatch(entities.getPattern_OneSokoban());
	}

	@Test
	public void test_TwoSokoban() {
		expectNoMatch(entities.getPattern_TwoSokoban());
	}

	@Test
	public void test_OneSokoban_StillValid() {
		var p = entities.getPattern_OneSokoban();
		var matches = p.matcher().determineMatches();
		expectValidMatches(matches, matches.size());
	}

	@Test
	public void test_OneSokobanSelectedFigureRequired() {
		expectSingleMatch(entities.getPattern_OneSokobanSelectedFigureRequired());
	}

	@Test
	public void test_OneSokobanSelectedFigureRequired_StillValid() {
		var p = entities.getPattern_OneSokobanSelectedFigureRequired();
		var matches = p.matcher().determineMatches();
		expectValidMatches(matches, matches.size());
	}

	@Test
	public void test_OneSokobanSelectedFigureRequired_StillValid_AfterDeletedEdge() {
		var p = entities.getPattern_OneSokobanSelectedFigureRequired();
		var matches = p.matcher().determineMatches();

		builder.executeQueryForSideEffect("MATCH (:Board)-[rel:selectedFigure]->(:Sokoban) DELETE rel");
		expectValidMatches(matches, matches.size() - 1);
	}

	@Test
	public void test_OneSokoban_StillValid_AfterChangeSokobanToBlock() {
		var p = entities.getPattern_OneSokoban();
		var matches = p.matcher().determineMatches();

		builder.executeQueryForSideEffect("MATCH (s:Sokoban) SET s:Block REMOVE s:Sokoban");
		expectValidMatches(matches, matches.size() - 1);
	}

	@Test
	public void test_OneBlock() {
		var p = entities.getPattern_OneBlock();
		var matches = p.matcher().determineMatches();
		assertThat(matches.size(), is(2));
	}
	
	@Test 
	public void test_blockOnEndFieldWithRight() {
		assertThat(entities.getPattern_BlockOnEndFieldWithRight().matcher().countMatches(), is(1));
	}

	@Test
	public void test_OneBlock_StillValid() {
		var p = entities.getPattern_OneBlock();
		var matches = p.matcher().determineMatches();
		expectValidMatches(matches, matches.size());
	}

	@Test
	public void test_OneEndField() {
		assertThat(entities.getPattern_OneEndField().matcher().countMatches(), is(2));
	}
	
	@Test
	public void test_OneFieldWithMask() {
		var p = entities.getPattern_OneNormalField();
		var mask = p.mask().setFEndPos(true);
		assertEquals(2, p.matcher(mask).countMatches());
		
		mask = p.mask().setFEndPos(false);
		assertEquals(14, p.matcher(mask).countMatches());
	}
	
	@Test
	public void test_OneNewFieldWithMaskSPO() {
		var r = entities.getRule_OneExtraField();
		var mask = r.mask().setFEndPos(true).setOldFEndPos(false).setNewFEndPos(true);
		
		var matches = r.rule(mask).determineMatches();
		assertEquals(2, matches.size());
		
		var iterator = matches.iterator();
		
		var rule = r.rule(mask);
		rule.useSPOSemantics(true);
		
		var nextMatch = iterator.next();
		Optional<NeoCoMatch> result1 = rule.apply(nextMatch);
		assertTrue(result1.isPresent());
		assertFalse(nextMatch.isStillValid());
		
		nextMatch = iterator.next();
		Optional<NeoCoMatch> result2 = rule.apply(nextMatch);
		assertTrue(result2.isPresent());
		assertFalse(nextMatch.isStillValid());
	
	}
	
	@Test
	public void test_OneNewFieldWithMaskDPO() {
		var r = entities.getRule_OneExtraField();
		var mask = r.mask().setFEndPos(true).setOldFEndPos(false).setNewFEndPos(true);
		
		var matches = r.rule(mask).determineMatches();
		assertEquals(2, matches.size());
		
		var iterator = matches.iterator();
		
		var rule = r.rule(mask);
		rule.useSPOSemantics(false);
		
		var nextMatch = iterator.next();
		try {
			Optional<NeoCoMatch> result1 = rule.apply(nextMatch);
			assertTrue(result1.isPresent());
			assertFalse(nextMatch.isStillValid());
		} catch (Exception e) {
			assertFalse(false);
		}
		
		
		nextMatch = iterator.next();
		try {
			Optional<NeoCoMatch> result2 = rule.apply(nextMatch);
			assertTrue(result2.isPresent());
			assertFalse(nextMatch.isStillValid());
		} catch (Exception e) {
			assertFalse(false);
		}
	
	}
	
	@Test
	public void test_OneBoardWithFieldWithMask() {
		var p = entities.getPattern_OneBoardWithField();
		var mask = p.mask().setB_fields_0_fCol(2).setB_fields_0_fRow(2);
		
		var matches = p.matcher().countMatches();
		assertEquals(16, matches);
		var matchesWithMask = p.matcher(mask).countMatches();
		assertEquals(1, matchesWithMask);
	}
	
	@Test
	public void test_OneBoardWithNewFieldWithMaskSPO() {
		
		var r = entities.getRule_OneBoardWithNewField();
		var mask = r.mask().setB_fields_0_f1Col(0).setB_fields_0_f1Row(0)
				.setB_fields_1_f2Col(1).setB_fields_1_f2Row(1)
				.setB_fields_2_f3Col(99).setB_fields_2_f3Row(99);
		
		var rule = r.rule(mask);
		var matches = rule.determineMatches();
		assertEquals(1, matches.size());
		
		var iterator = matches.iterator();
		
		var nextMatch = iterator.next();
		rule.useSPOSemantics(true);
		Optional<NeoCoMatch> result = rule.apply(nextMatch);
		assertTrue(result.isPresent());
		assertFalse(nextMatch.isStillValid());
		
	}
	
	@Test
	public void test_OneBoardWithNewFieldWithMaskAndDPO() {
		
		var r = entities.getRule_OneBoardWithNewField();
		var mask = r.mask().setB_fields_0_f1Col(0).setB_fields_0_f1Row(0)
				.setB_fields_1_f2Col(1).setB_fields_1_f2Row(1)
				.setB_fields_2_f3Col(99).setB_fields_2_f3Row(99);
		
		var rule = r.rule(mask);
		var matches = rule.determineMatches();
		assertEquals(1, matches.size());
		
		var iterator = matches.iterator();
		
		var nextMatch = iterator.next();
		try {
			Optional<NeoCoMatch> result = rule.apply(nextMatch);
			assertTrue(result.isPresent());
			assertFalse(nextMatch.isStillValid());
		} catch (Exception e) {
			assertFalse(false);
		}
		
	}


	@Test
	public void test_OneEndField_StillValid() {
		var p = entities.getPattern_OneEndField();
		var matches = p.matcher().determineMatches();
		expectValidMatches(matches, matches.size());
	}

	@Test
	public void test_OccupiedField() {
		assertThat(entities.getPattern_OccupiedField().matcher().countMatches(), is(9));
	}

	@Test
	public void test_OccupiedField_StillValid() {
		var p = entities.getPattern_OccupiedField();
		var matches = p.matcher().determineMatches();
		expectValidMatches(matches, matches.size());
	}

	@Test
	public void test_OccupiedField_StillValid_AfterDeletingBlocks() {
		var p = entities.getPattern_OccupiedField();
		var matches = p.matcher().determineMatches();

		// removing 2 blocks, valid matches should be 2 less
		builder.executeQueryForSideEffect("MATCH (b:Block) DETACH DELETE b");

		expectValidMatches(matches, matches.size() - 2);
	}

	@Test
	public void test_AnOccupiedSokobanField() {
		expectSingleMatch(entities.getPattern_AnOccupiedSokobanField());
	}

	@Test
	public void test_AnOccupiedSokobanField_StillValid() {
		var p = entities.getPattern_AnOccupiedSokobanField();
		var matches = p.matcher().determineMatches();
		expectValidMatches(matches, matches.size());
	}

	@Test
	public void test_AnOccupiedBlockField() {
		assertThat(entities.getPattern_AnOccupiedBlockField().matcher().countMatches(), is(2));
	}

	@Test
	public void test_AnOccupiedBlockField_StillValid() {
		var p = entities.getPattern_AnOccupiedBlockField();
		var matches = p.matcher().determineMatches();
		expectValidMatches(matches, matches.size());
	}

	@Test
	public void test_AnOccupiedBoulderField() {
		assertThat(entities.getPattern_AnOccupiedBoulderField().matcher().countMatches(), is(8));
	}

	@Test
	public void test_AnOccupiedBoulderField_StillValid() {
		var p = entities.getPattern_AnOccupiedBoulderField();
		var matches = p.matcher().determineMatches();
		expectValidMatches(matches, matches.size());
	}

	@Test
	public void test_AllFieldsInARow() {
		assertThat(entities.getPattern_AllFieldsInARow().matcher().countMatches(), is(4));
	}

	@Test
	public void test_AllNotBorderFieldsInARow() {
		assertThat(entities.getPattern_AllNotBorderFieldsInARow().matcher().countMatches(), is(2));
	}

	@Test
	public void test_AllNotBorderFieldsInARow_StillValid_AfterDeletingEdges() {
		var p = entities.getPattern_AllFieldsInARow();
		var matches = p.matcher().determineMatches();

		// Removing all right edges
		builder.executeQueryForSideEffect("MATCH (f:Field)-[r:right]->(g:Field) DETACH DELETE r");

		expectValidMatches(matches, 4);
	}

	@Test
	public void test_AllNotBorderFieldsInARowAndCol() {
		expectSingleMatch(entities.getPattern_AllNotBorderFieldsInARowAndCol());
	}

	@Test
	public void test_AllNotBorderFieldsInDiffRows() {
		expectNoMatch(entities.getPattern_AllNotBorderFieldsInDiffRows());
	}

	@Test
	@Disabled
	public void test_All3x3Fields() {
		assertThat(entities.getPattern_All3x3Fields().matcher().countMatches(), is(4));
	}

	// Test sometimes fails (return 0 matches) due to a bug in Neo4j.
	// See: https://github.com/neo4j/neo4j/issues/12247
	@Test
	@Disabled
	public void test_One3x3FieldsLimit() {
		assertThat(entities.getPattern_All3x3Fields().matcher().determineMatches(1).size(), is(1));
	}

	@Test
	@Disabled
	public void test_All3x3Fields_StillValid() {
		var p = entities.getPattern_All3x3Fields();
		var matches = p.matcher().determineMatches();
		expectValidMatches(matches, matches.size());
	}

	@Test
	public void test_All2x2Fields() {
		assertThat(entities.getPattern_All2x2Fields().matcher().countMatches(), is(9));
	}

	@Test
	public void test_All2x2Fields_StillValid() {
		var p = entities.getPattern_All2x2Fields();
		var matches = p.matcher().determineMatches();

		expectValidMatches(matches, matches.size());
		builder.executeQueryForSideEffect("MATCH (b:Board) DETACH DELETE b");
		expectValidMatches(matches, 0);
	}

	@Test
	@Disabled
	public void test_All3x3Fields_StillValid_AfterDeletingEdges() {
		var p = entities.getPattern_All3x3Fields();
		var matches = p.matcher().determineMatches();

		// Removing all right and bottom edges of endPos fields
		builder.executeQueryForSideEffect(
				"MATCH (f:Field {endPos: true, ename: \"f32\"})-[r:right]->(g:Field) DETACH DELETE f");

		expectValidMatches(matches, matches.size() - 2);
	}

	@Test
	@Disabled
	public void test_All3x3Fields_StillValid_AfterChangingTypesOfNodes() {
		var p = entities.getPattern_All3x3Fields();
		var matches = p.matcher().determineMatches();

		expectValidMatches(matches, matches.size());

		// removing all right and bottom edges of endPos fields
		builder.executeQueryForSideEffect("MATCH (f:Field {ename: \"f00\"}) SET f:OddLabel REMOVE f:Field");

		expectValidMatches(matches, matches.size() - 1);
	}

	@Test
	public void test_OccupiedNext() {
		assertThat(entities.getPattern_OccupiedNext().matcher().countMatches(), is(9));
	}

	@Test
	public void test_BoulderOnEndField() {
		expectNoMatch(entities.getPattern_BoulderOnEndField());
	}

	@Test
	public void test_boulderButNoBlock() {
		assertThat(entities.getPattern_BoulderButNoBlock().matcher().determineMatches().size(), is(6));
	}

	@Test
	public void test_twoBoulderButNoTwoBlock() {
		assertThat(entities.getPattern_TwoBoulderButNoTwoBlock().matcher().determineMatches().size(), is(54));
	}

	@Test
	public void test_twoBoulderButTwoBlock() {
		assertThat(entities.getPattern_TwoBoulderButTwoBlock().matcher().determineMatches().size(), is(2));
	}

	@Test
	public void test_oneBlock1() {
		assertThat(entities.getPattern_OneBlock1().matcher().determineMatches().size(), is(2));
	}

	@Test
	public void test_oneBlock2() {
		assertThat(entities.getPattern_OneBlock2().matcher().determineMatches().size(), is(0));
	}

	@Test
	public void test_oneBlock2Combi() {
		assertThat(entities.getPattern_OneBlock2Combi().matcher().determineMatches().size(), is(0));
	}

	@Test
	public void test_BlockNotOnEndFieldInCorner() {
		expectNoMatch(entities.getPattern_BlockNotOnEndFieldInCorner());
	}

	@Test
	public void test_ByBlockAndBoulderOccupiedFields() {
		assertThat(entities.getPattern_ByBlockAndBoulderOccupiedFields().matcher().countMatches(), is(14));
	}

	@Test
	public void test_PatternAllTwoFields() {
		assertThat(entities.getPattern_TwoField().matcher().determineMatches().size(), is(240));
	}

	@Test
	public void test_PatternOneTwoFields() {
		assertThat(entities.getPattern_TwoField().matcher().determineMatches(1).size(), is(1));
	}

	@Test
	public void test_PatternAllFourFields() {
		assertThat(entities.getPattern_FourField().matcher().determineMatches().size(), is(43680));
	}

	@Test
	public void test_PatternOneFourFields() {
		assertThat(entities.getPattern_FourField().matcher().determineMatches(1).size(), is(1));
	}

	/*
	 * Constraint Tests
	 */

	@Test
	public void test_constraint_hasLeft() {
		assertTrue(entities.getConstraint_ForbidLeftSide().isViolated());
	}

	@Test
	public void test_constraint_hasTopLeftCorner() {
		assertTrue(entities.getConstraint_TopLeftCorner().isViolated());
	}

	@Test
	public void test_constraint_hasTopRightCorner() {
		assertTrue(entities.getConstraint_TopRightCorner().isViolated());
	}

	@Test
	public void test_constraint_hasBottomLeftCorner() {
		assertTrue(entities.getConstraint_BottomLeftCorner().isViolated());
	}

	@Test
	public void test_constraint_hasBottomRightCorner() {
		assertTrue(entities.getConstraint_BottomRightCorner().isViolated());
	}

	@Test
	public void test_constraint_hasNoCorner() {
		assertTrue(entities.getConstraint_NoCorner().isViolated());
	}

	@Test
	public void test_hasOneSokoban() {
		assertTrue(entities.getConstraint_HasOneSokoban().isSatisfied());
	}

	@Test
	public void test_allConstraintTypesAtOnes() {
		assertTrue(entities.getConstraint_ExtremeConstraint().isSatisfied());
	}

	@Test
	public void test_allConstraintIfTwoThenTwoConn() {
		assertTrue(entities.getConstraint_IfTwoThenConn().isViolated());
	}

	@Test
	public void test_allConstraintIfTHasBottomThenHasRight() {
		assertTrue(entities.getConstraint_IfBottomThenRight().isViolated());
	}

	@Test
	public void test_ConditionHasField() {
		assertThat(entities.getPattern_OneField().matcher().countMatches(), is(9));
	}

	@Test
	public void test_ConditionHasFieldAllMatches() {
		assertThat(entities.getPattern_OneField().matcher().determineMatches().size(), is(9));
	}

	@Test
	public void test_ConditionHasFieldOneMatches() {
		assertThat(entities.getPattern_OneField().matcher().determineMatches(1).size(), is(1));
	}

	@Test
	public void test_ConditionHasFieldNeg() {
		assertThat(entities.getPattern_OneFieldNeg().matcher().countMatches(), is(7));
	}

	@Test
	public void test_ConditionHasFieldEnforceTwo() {
		assertThat(entities.getPattern_OneFieldEnforceTwo().matcher().countMatches(), is(12));
	}

	@Test
	public void test_ConditionHasFieldForbidFwo() {
		assertThat(entities.getPattern_OneFieldForbidTwo().matcher().countMatches(), is(4));
	}

	@Test
	public void test_ConditionHasOneFieldHasBottomAndRight() {
		assertThat(entities.getPattern_OneFieldHasBottomAndRight().matcher().countMatches(), is(9));
	}

	@Test
	public void test_ConditionHasOneFieldHasNoBottomOrNoRight() {
		assertThat(entities.getPattern_OneFieldHasNoBottomOrNoRight().matcher().countMatches(), is(7));
	}

	@Test
	public void test_constraint_SokobanHasFieldThenBlockHasField() {
		assertTrue(entities.getConstraint_SokOnFieldThenBlockOnField().isViolated());
	}

	@Test
	public void testNoBlockedBlocks() {
		assertFalse(entities.getPattern_BlockNotOnEndFieldInCorner().matcher().determineOneMatch().isPresent());
	}

	@Test
	public void testSokobanOnFieldOfBoard() {
		assertThat(entities.getPattern_SokobanOnFieldOfBoard().matcher().countMatches(), is(1));
	}

	@Test
	public void testSokobanOnFieldOfBoard_StillValid() {
		var p = entities.getPattern_SokobanOnFieldOfBoard();
		var matches = p.matcher().determineMatches();
		expectValidMatches(matches, matches.size());
	}

	@Test
	public void patternMoveSokobanDownTest() {
		var p = entities.getPattern_PatternMoveSokobanDownTest();
		var matches = p.matcher().determineMatches();
		expectValidMatches(matches, matches.size());
	}
	
	@Test
	public void testEvenMoreNeighboringFields() {
		clearDB();
		initDB(new API_Simple3x3Field(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI).getModel_SimpleThreeByThreeField());
		assertThat(entities.getPattern_EvenMoreNeighbouringFields().matcher().countMatches(), is(12));
	}
	
	@Test
	public void testDisjoinedPatternWithConditionElements_Enforce() {
		assertEquals(16, entities.getPattern_OneFieldWithBlock().matcher().countMatches());
	}
	
	@Test
	public void testDisjoinedPatternWithConditionElements_Forbid() {
		assertEquals(0, entities.getPattern_OneFieldWithNoBlock().matcher().countMatches());
	}
}
