package org.emoflon.neo.example.chess;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.emoflon.neo.api.API_ChessBoard;
import org.emoflon.neo.api.API_ChessPatterns;
import org.emoflon.neo.api.API_FigureMoves;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.example.ENeoTest;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;
import org.junit.jupiter.api.Test;

class ChessTests extends ENeoTest {

	private API_FigureMoves figureMoves = new API_FigureMoves(builder);
	private API_ChessBoard models = new API_ChessBoard(builder);
	private API_ChessPatterns patterns = new API_ChessPatterns(builder);
	
	@Test
	void test_AllDiagonalReferences() {
		initDB(models.getModel_CompleteBoard());
		assertThat(figureMoves.getPattern_BottomLeftReference().matcher().countMatches(), is(49));
		assertThat(figureMoves.getPattern_BottomRightReference().matcher().countMatches(), is(49));
	}
	
	@Test
	void test_CaroKannDefense() {
		initDB(models.getModel_CaroKannDefense());
		expectSingleMatch(patterns.getPattern_CaroKannDefense());
	}
	
	@Test
	void test_KingsGambit() {
		initDB(models.getModel_KingsGambit());
		expectSingleMatch(patterns.getPattern_KingsGambit());
	}
	
	@Test
	void move_WhitePawn() {
		initDB(models.getModel_PawnOnBoard());
		IRule<NeoMatch, NeoCoMatch> rule = figureMoves.getRule_MoveWhitePawn().rule();
		var matches = rule.determineMatches();
		assertTrue(matches.size() == 2);
		
		var onlyMatch = matches.iterator().next();
		
		Optional<NeoCoMatch> result = rule.apply(onlyMatch);
		assertTrue(result.isPresent());
		
		expectInvalidMatch(onlyMatch);
	}

	@Test
	void move_WhitePawnByRefinement() {
		initDB(models.getModel_PawnOnBoard());
		IRule<NeoMatch, NeoCoMatch> rule = figureMoves.getRule_MovePawnByRefinement().rule();
		var matches = rule.determineMatches();
		assertTrue(matches.size() == 2);
		
		Optional<NeoCoMatch> result = rule.apply(matches.iterator().next());
		assertTrue(result.isPresent());
		expectInvalidMatch((NeoMatch) matches.toArray()[0]);
	}
}
