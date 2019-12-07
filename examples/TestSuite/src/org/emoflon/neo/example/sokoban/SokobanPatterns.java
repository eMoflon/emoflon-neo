package org.emoflon.neo.example.sokoban;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Optional;

import org.emoflon.neo.api.models.API_Simple3x3Field;
import org.emoflon.neo.api.models.API_SokobanSimpleTestField;
import org.emoflon.neo.api.rules.API_SokobanPatternsRulesConstraints;
import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.Schedule;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SokobanPatterns extends ENeoTest {

	private API_SokobanPatternsRulesConstraints entities = new API_SokobanPatternsRulesConstraints(builder);

	@BeforeEach
	public void initDB() {
		initDB(new API_SokobanSimpleTestField(builder).getModel_SokobanSimpleTestField());
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
		var matches = p.pattern().determineMatches();
		expectValidMatches(matches, matches.size());
	}

	@Test
	public void test_OneSokobanSelectedFigureRequired() {
		expectSingleMatch(entities.getPattern_OneSokobanSelectedFigureRequired());
	}

	@Test
	public void test_OneSokobanSelectedFigureRequired_StillValid() {
		var p = entities.getPattern_OneSokobanSelectedFigureRequired();
		var matches = p.pattern().determineMatches();
		expectValidMatches(matches, matches.size());
	}

	@Test
	public void test_OneSokobanSelectedFigureRequired_StillValid_AfterDeletedEdge() {
		var p = entities.getPattern_OneSokobanSelectedFigureRequired();
		var matches = p.pattern().determineMatches();

		builder.executeQueryForSideEffect("MATCH (:SokobanLanguage__Board)-[rel:selectedFigure]->(:SokobanLanguage__Sokoban) DELETE rel");
		expectValidMatches(matches, matches.size() - 1);
	}

	@Test
	public void test_OneSokoban_StillValid_AfterChangeSokobanToBlock() {
		var p = entities.getPattern_OneSokoban();
		var matches = p.pattern().determineMatches();

		builder.executeQueryForSideEffect("MATCH (s:SokobanLanguage__Sokoban) SET s:Block REMOVE s:SokobanLanguage__Sokoban");
		expectValidMatches(matches, matches.size() - 1);
	}
	
	@Test 
	public void  test_allEndFieldValid() {
		var p = entities.getPattern_OneEndField().pattern();
		var matches = p.determineMatches();
		assertEquals(2, matches.size());
		var tempMatches = p.isStillValid(matches);
		
		var validMatches = new ArrayList<NeoMatch>(matches);
		for(var match : matches) {
			if(tempMatches.containsKey(match.getMatchID()) && !tempMatches.get(match.getMatchID())) {
				validMatches.remove(match);
			}
		}
		matches = validMatches;
		assertEquals(2, matches.size());
	}
	
	@Test 
	public void  test_allEndFieldValidWithSideEffects() {
		var p = entities.getPattern_OneEndField().pattern();
		var matches = p.determineMatches();
		assertEquals(2, matches.size());
		builder.executeQueryForSideEffect("MATCH (f:SokobanLanguage__Field {endPos:true})-[:bottom]->(f2:SokobanLanguage__Field {endPos:true}) SET f2.endPos = false");
		var tempMatches = p.isStillValid(matches);
		
		var validMatches = new ArrayList<NeoMatch>(matches);
		for(var match : matches) {
			if(tempMatches.containsKey(match.getMatchID()) && !tempMatches.get(match.getMatchID())) {
				validMatches.remove(match);
			}
		}
		matches = validMatches;
		assertEquals(1, matches.size());
	}
	
	@Test 
	public void  test_oneNormalField() {
		var p = entities.getPattern_OneNormalField().pattern();
		var matches = p.determineMatches();
		assertEquals(12, matches.size());
		
		var tempMatches = p.isStillValid(matches);
		
		var validMatches = new ArrayList<NeoMatch>(matches);
		for(var match : matches) {
			if(tempMatches.containsKey(match.getMatchID()) && !tempMatches.get(match.getMatchID())) {
				validMatches.remove(match);
			}
		}
		matches = validMatches;
		assertEquals(12, matches.size());
	}
	
	@Test 
	public void  test_oneNormalFieldWithSideEffects() {
		var p = entities.getPattern_OneNormalField().pattern();
		var matches = p.determineMatches();
		assertEquals(12, matches.size());
		
		builder.executeQueryForSideEffect("MATCH (f:SokobanLanguage__Field {endPos:true})-[rel:right]->(f2:SokobanLanguage__Field) DELETE rel");
		var tempMatches = p.isStillValid(matches);
		
		var validMatches = new ArrayList<NeoMatch>(matches);
		for(var match : matches) {
			if(tempMatches.containsKey(match.getMatchID()) && !tempMatches.get(match.getMatchID())) {
				validMatches.remove(match);
			}
		}
		matches = validMatches;
		assertEquals(10, matches.size());
	}
	
	@Test 
	public void  test_oneNormalFieldNeg() {
		var p = entities.getPattern_OneNormalFieldNeg().pattern();
		var matches = p.determineMatches();
		assertEquals(4, matches.size());
		
		var tempMatches = p.isStillValid(matches);
		
		var validMatches = new ArrayList<NeoMatch>(matches);
		for(var match : matches) {
			if(tempMatches.containsKey(match.getMatchID()) && !tempMatches.get(match.getMatchID())) {
				validMatches.remove(match);
			}
		}
		matches = validMatches;
		assertEquals(4, matches.size());
	}
	
	@Test 
	public void  test_oneNormalFieldNegWithSideEffects() {
		var p = entities.getPattern_OneNormalFieldNeg().pattern();
		var matches = p.determineMatches();
		assertEquals(4, matches.size());
		
		builder.executeQueryForSideEffect("MATCH (f:SokobanLanguage__Field)-[:bottom]->(f2:SokobanLanguage__Field) CREATE (f)-[:right]->(f2)");
		var tempMatches = p.isStillValid(matches);
		
		var validMatches = new ArrayList<NeoMatch>(matches);
		for(var match : matches) {
			if(tempMatches.containsKey(match.getMatchID()) && !tempMatches.get(match.getMatchID())) {
				validMatches.remove(match);
			}
		}
		matches = validMatches;
		assertEquals(1, matches.size());
	}

	@Test
	public void test_OneBlock() {
		var p = entities.getPattern_OneBlock();
		var matches = p.pattern().determineMatches();
		assertThat(matches.size(), is(2));
	}
	
	@Test 
	public void test_blockOnEndFieldWithRight() {
		assertThat(entities.getPattern_BlockOnEndFieldWithRight().pattern().countMatches(), is(1));
	}

	@Test
	public void test_OneBlock_StillValid() {
		var p = entities.getPattern_OneBlock();
		var matches = p.pattern().determineMatches();
		expectValidMatches(matches, matches.size());
	}

	@Test
	public void test_OneEndField() {
		assertThat(entities.getPattern_OneEndField().pattern().countMatches(), is(2));
	}
	
	@Test
	public void test_OneFieldWithMask() {
		var p = entities.getPattern_OneNormalField();
		var mask = p.mask().setFEndPos(true);
		assertEquals(2, p.countMatches(mask));
		
		mask = p.mask().setFEndPos(false);
		assertEquals(10, p.countMatches(mask));
	}
	
	@Test
	public void test_OneFieldWithMaskValid() {
		var p = entities.getPattern_OneNormalField();
		var mask = p.mask().setFEndPos(true);
		
		var matches = p.determineMatches(mask);
		assertEquals(2, matches.size());
		
		var tempMatches = p.pattern().isStillValid(matches);
		var validMatches = new ArrayList<NeoMatch>(matches);
		for(var match : matches) {
			if(tempMatches.containsKey(match.getMatchID()) && !tempMatches.get(match.getMatchID())) {
				validMatches.remove(match);
			}
		}
		matches = validMatches;
		assertEquals(2, matches.size());
		
		
		var mask2 = p.mask().setFEndPos(false);
		var matches2 = p.determineMatches(mask2);
		assertEquals(10, matches2.size());
		
		var tempMatches2 = p.pattern().isStillValid(matches2);
		var validMatches2 = new ArrayList<NeoMatch>(matches2);
		for(var match : matches2) {
			if(tempMatches2.containsKey(match.getMatchID()) && !tempMatches2.get(match.getMatchID())) {
				validMatches2.remove(match);
			}
		}
		matches2 = validMatches2;
		assertEquals(10, matches2.size());
	}
	
	@Test
	public void test_OneNewFieldWithMaskSPO() {
		var r = entities.getRule_OneExtraField();
		var mask = r.mask().setFEndPos(true).setOldFEndPos(false).setNewFEndPos(true);
		
		var matches = r.determineMatches(mask);
		assertEquals(2, matches.size());
		
		var iterator = matches.iterator();
		
		var rule = r.rule();
		rule.setSPOSemantics(true);
		
		var nextMatch = iterator.next();
		Optional<NeoCoMatch> result1 = r.apply(nextMatch, mask);
		assertTrue(result1.isPresent());
		expectInvalidMatch(nextMatch);
		
		nextMatch = iterator.next();
		Optional<NeoCoMatch> result2 = rule.apply(nextMatch);
		assertTrue(result2.isPresent());
		expectInvalidMatch(nextMatch);	
	}
	
	@Test
	public void test_OneNewFieldWithMaskDPO() {
		var r = entities.getRule_OneExtraField();
		var mask = r.mask().setFEndPos(true).setOldFEndPos(false).setNewFEndPos(true);
		
		var matches = r.determineMatches(mask);
		assertEquals(2, matches.size());
		
		var iterator = matches.iterator();
		
		var rule = r.rule();
		rule.setSPOSemantics(false);
		
		var nextMatch = iterator.next();
		try {
			Optional<NeoCoMatch> result1 = r.apply(nextMatch, mask);
			assertTrue(result1.isPresent());
			expectInvalidMatch(nextMatch);
		} catch (Exception e) {
			assertFalse(false);
		}
		
		
		nextMatch = iterator.next();
		try {
			Optional<NeoCoMatch> result2 = r.apply(nextMatch, mask);
			assertTrue(result2.isPresent());
			expectInvalidMatch(nextMatch);
		} catch (Exception e) {
			assertFalse(false);
		}
	
	}
	
	@Test
	public void test_OneBoardWithFieldWithMask() {
		var p = entities.getPattern_OneBoardWithField();
		var mask = p.mask().setB_fields_0_fCol(2).setB_fields_0_fRow(2);
		
		var matches = p.pattern().countMatches();
		assertEquals(16, matches);
		var matchesWithMask = p.countMatches(mask);
		assertEquals(1, matchesWithMask);
	}
	
	@Test
	public void test_OneBoardWithNewFieldWithMaskSPO() {
		
		var r = entities.getRule_OneBoardWithNewField();
		var mask = r.mask().setB_fields_0_f1Col(0).setB_fields_0_f1Row(0)
				.setB_fields_1_f2Col(1).setB_fields_1_f2Row(1)
				.setB_fields_2_f3Col(99).setB_fields_2_f3Row(99);
		
		var rule = r.rule();
		var matches = r.determineMatches(mask);
		assertEquals(1, matches.size());
		
		var iterator = matches.iterator();
		
		var nextMatch = iterator.next();
		rule.setSPOSemantics(true);
		Optional<NeoCoMatch> result = rule.apply(nextMatch);
		assertTrue(result.isPresent());
		expectInvalidMatch(nextMatch);
	}
	
	@Test
	public void test_OneBoardWithNewFieldWithMaskAndDPO() {
		
		var r = entities.getRule_OneBoardWithNewField();
		var mask = r.mask().setB_fields_0_f1Col(0).setB_fields_0_f1Row(0)
				.setB_fields_1_f2Col(1).setB_fields_1_f2Row(1)
				.setB_fields_2_f3Col(99).setB_fields_2_f3Row(99);
		
		var matches = r.determineMatches(mask);
		assertEquals(1, matches.size());
		
		var iterator = matches.iterator();
		
		var nextMatch = iterator.next();
		try {
			Optional<NeoCoMatch> result = r.apply(nextMatch, mask);
			assertTrue(result.isPresent());
			expectInvalidMatch(nextMatch);
		} catch (Exception e) {
			assertFalse(false);
		}
		
	}


	@Test
	public void test_OneEndField_StillValid() {
		var p = entities.getPattern_OneEndField();
		var matches = p.pattern().determineMatches();
		expectValidMatches(matches, matches.size());
	}

	@Test
	public void test_OccupiedField() {
		assertThat(entities.getPattern_OccupiedField().pattern().countMatches(), is(9));
	}

	@Test
	public void test_OccupiedField_StillValid() {
		var p = entities.getPattern_OccupiedField();
		var matches = p.pattern().determineMatches();
		expectValidMatches(matches, matches.size());
	}

	@Test
	public void test_OccupiedField_StillValid_AfterDeletingBlocks() {
		var p = entities.getPattern_OccupiedField();
		var matches = p.pattern().determineMatches();

		// removing 2 blocks, valid matches should be 2 less
		builder.executeQueryForSideEffect("MATCH (b:SokobanLanguage__Block) DETACH DELETE b");

		expectValidMatches(matches, matches.size() - 2);
	}
	
	@Test
	public void test_ConnectedEndField_StillValid_AfterOneNoEndField() {
		var p = entities.getPattern_TwoEmptyEndFields();
		var matches = p.pattern().determineMatches();
		expectSingleMatch(p);
		
		// removing 2 blocks, valid matches should be 2 less
		builder.executeQueryForSideEffect("MATCH (f1:SokobanLanguage__Field {endPos:true})-[:bottom]->(f2:SokobanLanguage__Field {endPos: true}) SET f2.endPos = false");

		expectValidMatches(matches, matches.size() - 1);
	}


	@Test
	public void test_AnOccupiedSokobanField() {
		expectSingleMatch(entities.getPattern_AnOccupiedSokobanField());
	}

	@Test
	public void test_AnOccupiedSokobanField_StillValid() {
		var p = entities.getPattern_AnOccupiedSokobanField();
		var matches = p.pattern().determineMatches();
		expectValidMatches(matches, matches.size());
	}

	@Test
	public void test_AnOccupiedBlockField() {
		assertThat(entities.getPattern_AnOccupiedBlockField().pattern().countMatches(), is(2));
	}

	@Test
	public void test_AnOccupiedBlockField_StillValid() {
		var p = entities.getPattern_AnOccupiedBlockField();
		var matches = p.pattern().determineMatches();
		expectValidMatches(matches, matches.size());
	}

	@Test
	public void test_AnOccupiedBoulderField() {
		assertThat(entities.getPattern_AnOccupiedBoulderField().pattern().countMatches(), is(8));
	}

	@Test
	public void test_AnOccupiedBoulderField_StillValid() {
		var p = entities.getPattern_AnOccupiedBoulderField();
		var matches = p.pattern().determineMatches();
		expectValidMatches(matches, matches.size());
	}

	@Test
	public void test_AllFieldsInARow() {
		assertThat(entities.getPattern_AllFieldsInARow().pattern().countMatches(), is(4));
	}

	@Test
	public void test_AllNotBorderFieldsInARow() {
		assertThat(entities.getPattern_AllNotBorderFieldsInARow().pattern().countMatches(), is(2));
	}

	@Test
	public void test_AllNotBorderFieldsInARow_StillValid_AfterDeletingEdges() {
		var p = entities.getPattern_AllFieldsInARow();
		var matches = p.pattern().determineMatches();

		// Removing all right edges
		builder.executeQueryForSideEffect("MATCH (f:SokobanLanguage__Field)-[r:right]->(g:SokobanLanguage__Field) DETACH DELETE r");

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
	public void test_All3x3Fields() {
		assertThat(entities.getPattern_All3x3Fields().pattern().countMatches(), is(4));
	}

	@Test
	public void test_One3x3FieldsLimit() {
		assertThat(entities.getPattern_All3x3Fields().pattern().determineMatches(Schedule.once()).size(), is(1));
	}

	@Test
	public void test_All3x3Fields_StillValid() {
		var p = entities.getPattern_All3x3Fields();
		var matches = p.pattern().determineMatches();
		expectValidMatches(matches, matches.size());
	}

	@Test
	public void test_All2x2Fields() {
		assertThat(entities.getPattern_All2x2Fields().pattern().countMatches(), is(9));
	}

	@Test
	public void test_All2x2Fields_StillValid() {
		var p = entities.getPattern_All2x2Fields();
		var matches = p.pattern().determineMatches();

		expectValidMatches(matches, matches.size());
		builder.executeQueryForSideEffect("MATCH (b:SokobanLanguage__Board) DETACH DELETE b");
		expectValidMatches(matches, 0);
	}

	@Test
	public void test_All3x3Fields_StillValid_AfterDeletingEdges() {
		var p = entities.getPattern_All3x3Fields();
		var matches = p.pattern().determineMatches();

		// Removing all right and bottom edges of endPos fields
		builder.executeQueryForSideEffect(
				"MATCH (f:SokobanLanguage__Field {endPos: true, ename: \"f32\"})-[r:right]->(g:SokobanLanguage__Field) DETACH DELETE f");

		expectValidMatches(matches, matches.size() - 2);
	}

	@Test
	public void test_All3x3Fields_StillValid_AfterChangingTypesOfNodes() {
		var p = entities.getPattern_All3x3Fields();
		var matches = p.pattern().determineMatches();

		expectValidMatches(matches, matches.size());

		// removing all right and bottom edges of endPos fields
		builder.executeQueryForSideEffect("MATCH (f:SokobanLanguage__Field {ename: \"f00\"}) SET f:OddLabel REMOVE f:SokobanLanguage__Field");

		expectValidMatches(matches, matches.size() - 1);
	}

	@Test
	public void test_OccupiedNext() {
		assertThat(entities.getPattern_OccupiedNext().pattern().countMatches(), is(9));
	}

	@Test
	public void test_BoulderOnEndField() {
		expectNoMatch(entities.getPattern_BoulderOnEndField());
	}

	@Test
	public void test_boulderButNoBlock() {
		assertThat(entities.getPattern_BoulderButNoBlock().pattern().determineMatches().size(), is(6));
	}

	@Test
	public void test_twoBoulderButNoTwoBlock() {
		assertThat(entities.getPattern_TwoBoulderButNoTwoBlock().pattern().determineMatches().size(), is(54));
	}

	@Test
	public void test_twoBoulderButTwoBlock() {
		assertThat(entities.getPattern_TwoBoulderButTwoBlock().pattern().determineMatches().size(), is(2));
	}

	@Test
	public void test_oneBlock1() {
		assertThat(entities.getPattern_OneBlock1().pattern().determineMatches().size(), is(2));
	}

	@Test
	public void test_oneBlock2() {
		assertThat(entities.getPattern_OneBlock2().pattern().determineMatches().size(), is(0));
	}

	@Test
	public void test_oneBlock2Combi() {
		assertThat(entities.getPattern_OneBlock2Combi().pattern().determineMatches().size(), is(0));
	}

	@Test
	public void test_BlockNotOnEndFieldInCorner() {
		expectNoMatch(entities.getPattern_BlockNotOnEndFieldInCorner());
	}

	@Test
	public void test_ByBlockAndBoulderOccupiedFields() {
		assertThat(entities.getPattern_ByBlockAndBoulderOccupiedFields().pattern().countMatches(), is(14));
	}

	@Test
	public void test_PatternAllTwoFields() {
		assertThat(entities.getPattern_TwoField().pattern().determineMatches().size(), is(240));
	}

	@Test
	public void test_PatternOneTwoFields() {
		assertThat(entities.getPattern_TwoField().pattern().determineMatches(Schedule.once()).size(), is(1));
	}

	@Test
	public void test_PatternAllFourFields() {
		assertThat(entities.getPattern_FourField().pattern().determineMatches().size(), is(43680));
	}

	@Test
	public void test_PatternOneFourFields() {
		assertThat(entities.getPattern_FourField().pattern().determineMatches(Schedule.once()).size(), is(1));
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
		assertThat(entities.getPattern_OneField().pattern().countMatches(), is(9));
	}

	@Test
	public void test_ConditionHasFieldAllMatches() {
		assertThat(entities.getPattern_OneField().pattern().determineMatches().size(), is(9));
	}

	@Test
	public void test_ConditionHasFieldOneMatches() {
		assertThat(entities.getPattern_OneField().pattern().determineMatches(Schedule.once()).size(), is(1));
	}

	@Test
	public void test_ConditionHasFieldNeg() {
		assertThat(entities.getPattern_OneFieldNeg().pattern().countMatches(), is(7));
	}

	@Test
	public void test_ConditionHasFieldEnforceTwo() {
		assertThat(entities.getPattern_OneFieldEnforceTwo().pattern().countMatches(), is(12));
	}

	@Test
	public void test_ConditionHasFieldForbidFwo() {
		assertThat(entities.getPattern_OneFieldForbidTwo().pattern().countMatches(), is(4));
	}

	@Test
	public void test_ConditionHasOneFieldHasBottomAndRight() {
		assertThat(entities.getPattern_OneFieldHasBottomAndRight().pattern().countMatches(), is(9));
	}

	@Test
	public void test_ConditionHasOneFieldHasNoBottomOrNoRight() {
		assertThat(entities.getPattern_OneFieldHasNoBottomOrNoRight().pattern().countMatches(), is(7));
	}

	@Test
	public void test_constraint_SokobanHasFieldThenBlockHasField() {
		assertTrue(entities.getConstraint_SokOnFieldThenBlockOnField().isViolated());
	}

	@Test
	public void testNoBlockedBlocks() {
		assertFalse(entities.getPattern_BlockNotOnEndFieldInCorner().pattern().determineOneMatch().isPresent());
	}

	@Test
	public void testSokobanOnFieldOfBoard() {
		assertThat(entities.getPattern_SokobanOnFieldOfBoard().pattern().countMatches(), is(1));
	}

	@Test
	public void testSokobanOnFieldOfBoard_StillValid() {
		var p = entities.getPattern_SokobanOnFieldOfBoard();
		var matches = p.pattern().determineMatches();
		expectValidMatches(matches, matches.size());
	}

	@Test
	public void patternMoveSokobanDownTest() {
		var p = entities.getPattern_PatternMoveSokobanDownTest();
		var matches = p.pattern().determineMatches();
		expectValidMatches(matches, matches.size());
	}
	
	@Test
	public void testEvenMoreNeighboringFields() {
		clearDB();
		initDB(new API_Simple3x3Field(builder).getModel_SimpleThreeByThreeField());
		assertThat(entities.getPattern_EvenMoreNeighbouringFields().pattern().countMatches(), is(12));
	}
	
	@Test
	public void testDisjoinedPatternWithConditionElements_Enforce() {
		assertEquals(16, entities.getPattern_OneFieldWithBlock().pattern().countMatches());
	}
	
	@Test
	public void testDisjoinedPatternWithConditionElements_Forbid() {
		assertEquals(0, entities.getPattern_OneFieldWithNoBlock().pattern().countMatches());
	}
	
	@Test
	public void testAttributeExpressionsPattern() {
		assertEquals(2, entities.getPattern_TestAttrExpression().pattern().countMatches());
	}
	
	@Test
	public void testAttributeExpressionsPositiveConstraint() {
		assertTrue(entities.getConstraint_TestConstraintAttrExpression().isSatisfied());
	}
	
	@Test
	public void testAttributeExpressionsNegativeConstraint() {
		assertTrue(entities.getConstraint_TestConstraintAttrExpressionNegativ().isViolated());
	}
	
	@Test
	public void testAttributeExpressionsNegatedConstraint() {
		assertTrue(entities.getConstraint_TestConstraintAttrExpressionNegated().isSatisfied());
	}
	
	@Test
	public void testAttributeExpressionsNestedConstraint() {
		assertTrue(entities.getConstraint_TestConcatenatedConstraintAttrExpression().isSatisfied());
	}
	
	@Test
	public void testAttributeExpressionsPatternWithPositiveCondition() {
		assertEquals(2, entities.getPattern_SomeField().pattern().countMatches());
	}
	
	@Test
	public void testAttributeAssignmentsInRules() {
		
		IRule<NeoMatch, NeoCoMatch> rule = entities.getRule_TestAttributeAssignmentsInRule().rule();
		var matches = rule.determineMatches();
		assertTrue(matches.size() == 2);
		
		var iterator = matches.iterator();
		
		while(iterator.hasNext()) {
			var onlyMatch = iterator.next();
			expectValidMatch(onlyMatch);
			
			Optional<NeoCoMatch> result = rule.apply(onlyMatch);
			assertTrue(result.isPresent());
		}
		
	}
	
	@Test 
	public void  test_attrCondField() {
		var p = entities.getPattern_AttrCondField().pattern();
		var matches = p.determineMatches();
		assertEquals(1, matches.size());
		
		var tempMatches = p.isStillValid(matches);
		
		var validMatches = new ArrayList<NeoMatch>(matches);
		for(var match : matches) {
			if(tempMatches.containsKey(match.getMatchID()) && !tempMatches.get(match.getMatchID())) {
				validMatches.remove(match);
			}
		}
		matches = validMatches;
		assertEquals(1, matches.size());
	}
	
	@Test 
	public void  test_attrCondFieldCond() {
		var p = entities.getPattern_AttrCondFieldZero().pattern();
		var matches = p.determineMatches();
		assertEquals(1, matches.size());
		
		var tempMatches = p.isStillValid(matches);
		
		var validMatches = new ArrayList<NeoMatch>(matches);
		for(var match : matches) {
			if(tempMatches.containsKey(match.getMatchID()) && !tempMatches.get(match.getMatchID())) {
				validMatches.remove(match);
			}
		}
		matches = validMatches;
		assertEquals(1, matches.size());
	}
	
	@Test 
	public void  test_attrCondFieldCondZero2() {
		var p = entities.getPattern_AttrCondFieldZero2().pattern();
		var matches = p.determineMatches();
		assertEquals(1, matches.size());
		
		var tempMatches = p.isStillValid(matches);
		
		var validMatches = new ArrayList<NeoMatch>(matches);
		for(var match : matches) {
			if(tempMatches.containsKey(match.getMatchID()) && !tempMatches.get(match.getMatchID())) {
				validMatches.remove(match);
			}
		}
		matches = validMatches;
		assertEquals(1, matches.size());
	}
	
	@Test
	public void test_notExtenedIfElseConstraintWithNewAttributesOnly() {
		var c = entities.getConstraint_IfFieldThenEndField();
		assertFalse(c.isSatisfied());
	}
	
	@Test
	public void test_notExtenedIfElseConstraintWithInjectivityOnly() {
		var c = entities.getConstraint_IfBoulderThenBlock();
		assertFalse(c.isSatisfied());
	}
	
	@Test
	public void test_ifSokobanSelectedFigureThenSokobanOnField() {
		var c = entities.getConstraint_IfSokobanSelectedFigureThenOnField();
		assertTrue(c.isSatisfied());
	}
	
	@Test
	public void test_twoOccupiedFields() {
		var p = entities.getPattern_Test_twoOccupiedFields();
		assertEquals(2, p.pattern().countMatches());
	}
}
