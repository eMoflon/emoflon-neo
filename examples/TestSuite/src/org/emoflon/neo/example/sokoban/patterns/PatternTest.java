package org.emoflon.neo.example.sokoban.patterns;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.emoflon.neo.api.API_Src_Models_SokobanSimpleTestField;
import org.emoflon.neo.api.API_Src_Rules_SokobanPatternsRulesConstraints;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PatternTest extends ENeoTest {

	private API_Src_Rules_SokobanPatternsRulesConstraints entities = new API_Src_Rules_SokobanPatternsRulesConstraints(builder);

	@BeforeEach
	public void initDB() {
		initDB(new API_Src_Models_SokobanSimpleTestField(builder).getModel_SokobanSimpleTestField());
	}
	
	@Test
	public void test_Condition() {
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
		var matches = p.determineMatches();
		expectValidMatches(matches, matches.size());
	}
	
	@Test
	public void test_OneSokoban_StillValid_AfterChangeSokobanToBlock() {
		var p = entities.getPattern_OneSokoban();
		var matches = p.determineMatches();
		
		builder.executeQueryForSideEffect("MATCH (s:Sokoban) SET s:Block REMOVE s:Sokoban");
		expectValidMatches(matches, matches.size()-1);
	}

	@Test
	public void test_OneBlock() {
		var p = entities.getPattern_OneBlock();
		var matches = p.determineMatches();
		assertThat(matches.size(), is(2));
	}

	@Test
	public void test_OneBlock_StillValid() {
		var p = entities.getPattern_OneBlock();
		var matches = p.determineMatches();
		expectValidMatches(matches, matches.size());
	}

	@Test
	public void test_OneEndField() {
		assertThat(entities.getPattern_OneEndField().countMatches(), is(2));
	}

	@Test
	public void test_OneEndField_StillValid() {
		var p = entities.getPattern_OneEndField();
		var matches = p.determineMatches();
		expectValidMatches(matches, matches.size());
	}

	@Test
	public void test_OccupiedField() {
		assertThat(entities.getPattern_OccupiedField().countMatches(), is(9));
	}

	@Test
	public void test_OccupiedField_StillValid() {
		var p = entities.getPattern_OccupiedField();
		var matches = p.determineMatches();
		expectValidMatches(matches, matches.size());
	}

	@Test
	public void test_OccupiedField_StillValid_AfterDeletingBlocks() {
		var p = entities.getPattern_OccupiedField();
		var matches = p.determineMatches();

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
		var matches = p.determineMatches();
		expectValidMatches(matches, matches.size());
	}

	@Test
	public void test_AnOccupiedBlockField() {
		assertThat(entities.getPattern_AnOccupiedBlockField().countMatches(), is(2));
	}

	@Test
	public void test_AnOccupiedBlockField_StillValid() {
		var p = entities.getPattern_AnOccupiedBlockField();
		var matches = p.determineMatches();
		expectValidMatches(matches, matches.size());
	}

	@Test
	public void test_AnOccupiedBoulderField() {
		assertThat(entities.getPattern_AnOccupiedBoulderField().countMatches(), is(8));
	}

	@Test
	public void test_AnOccupiedBoulderField_StillValid() {
		var p = entities.getPattern_AnOccupiedBoulderField();
		var matches = p.determineMatches();
		expectValidMatches(matches, matches.size());
	}

	@Test
	public void test_AllFieldsInARow() {
		assertThat(entities.getPattern_AllFieldsInARow().countMatches(), is(4));
	}

	@Test
	public void test_AllNotBorderFieldsInARow() {
		assertThat(entities.getPattern_AllNotBorderFieldsInARow().countMatches(), is(2));
	}

	@Test
	public void test_AllNotBorderFieldsInARow_StillValid_AfterDeletingEdges() {
		var p = entities.getPattern_AllFieldsInARow();
		var matches = p.determineMatches();

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
	public void test_All3x3Fields() {
		assertThat(entities.getPattern_All3x3Fields().countMatches(), is(4));
	}

	@Test
	public void test_All3x3Fields_StillValid() {
		var p = entities.getPattern_All3x3Fields();
		var matches = p.determineMatches();
		expectValidMatches(matches, matches.size());
	}
	
	@Test
	public void test_All2x2Fields() {
		assertThat(entities.getPattern_All2x2Fields().countMatches(), is(9));
	}

	@Test
	public void test_All2x2Fields_StillValid() {
		var p = entities.getPattern_All2x2Fields();
		var matches = p.determineMatches();

		expectValidMatches(matches, matches.size());
		builder.executeQueryForSideEffect("MATCH (b:Board) DETACH DELETE b");
		expectValidMatches(matches, 0);
	}

	@Test
	public void test_All3x3Fields_StillValid_AfterDeletingEdges() {
		var p = entities.getPattern_All3x3Fields();
		var matches = p.determineMatches();

		// Removing all right and bottom edges of endPos fields
		builder.executeQueryForSideEffect("MATCH (f:Field {endPos: true, name: \"f32\"})-[r:right]->(g:Field) DETACH DELETE f");

		expectValidMatches(matches, matches.size() - 2);
	}
	
	@Test
	public void test_All3x3Fields_StillValid_AfterChangingTypesOfNodes() {
		var p = entities.getPattern_All3x3Fields();
		var matches = p.determineMatches();

		// removing all right and bottom edges of endPos fields
		builder.executeQueryForSideEffect("MATCH (f:Field {name: \"f00\"}) SET f:OddLabel REMOVE f:Field");

		expectValidMatches(matches, matches.size() - 1);
	}

	@Test
	public void test_OccupiedNext() {
		assertThat(entities.getPattern_OccupiedNext().countMatches(), is(9));
	}
	
	@Test
	public void test_BoulderOnEndField() {
		expectNoMatch(entities.getPattern_BoulderOnEndField());
	}
	
	@Test
	public void test_BlockNotOnEndFieldInCorner() {
		assertThat(entities.getPattern_BlockNotOnEndFieldInCorner().countMatches(), is(2));
	}
	
	@Test
	public void test_ByBlockAndBoulderOccupiedFields() {
		assertThat(entities.getPattern_ByBlockAndBoulderOccupiedFields().countMatches(), is(14));
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

}
