package org.emoflon.neo.example.chess;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.emoflon.neo.api.API_ChessBoard;
import org.emoflon.neo.api.API_ChessLanguage;
import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.API_FigureMoves;
import org.emoflon.neo.api.API_Patterns;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.example.ENeoTest;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class ChessTests extends ENeoTest {
	
	private API_ChessLanguage language = new API_ChessLanguage(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI);
	private API_FigureMoves figureMoves = new API_FigureMoves(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI);
	private API_ChessBoard models = new API_ChessBoard(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI);
	private API_Patterns patterns = new API_Patterns(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI);

	@BeforeEach
	public void initDB() {
		//initDB(models.getModel_CompleteBoard());
	}
	
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
	
	//@Disabled
	@Test
	void move_WhitePawn() {
		initDB(models.getModel_PawnOnBoard());
		IRule<NeoMatch, NeoCoMatch> rule = figureMoves.getRule_MoveWhitePawn().rule();
		var matches = rule.determineMatches();
		assertTrue(matches.size() == 1);
		
		var onlyMatch = matches.iterator().next();
		
		Optional<NeoCoMatch> result = rule.apply(onlyMatch);
		assertTrue(result.isPresent());
		assertFalse(onlyMatch.isStillValid());
	}


}
