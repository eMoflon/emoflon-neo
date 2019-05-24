package org.emoflon.neo.example.she_remembered_caterpillars;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.emoflon.neo.api.API_Emsl_SheRememberedCaterpillars;
import org.emoflon.neo.emsl.eMSL.Model;
import org.emoflon.neo.example.ENeoTest;
import org.emoflon.neo.neo4j.adapter.NeoPattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class PatternTest extends ENeoTest {
	private API_Emsl_SheRememberedCaterpillars entities = new API_Emsl_SheRememberedCaterpillars(builder);
	private Model model = entities.getModel_SimpleGame();
	
	@BeforeEach
	private void initDB() {
		builder.exportEMSLEntityToNeo4j(model);
		logger.info("-----------------------------\n" + "Database initialised.");
	}
	
	@Test
	@Disabled("TODO[Jannik] Extend patterns to handle attribute expressions")
	public void testPatternWithAttributeExpression() {
		NeoPattern p = entities.getPattern_CanCrossBridge();
		var matches = p.getMatches();
		assertThat(matches.size(), is(1));
	}
	
	@Test
	@Disabled("TODO[Jannik] Extend patterns to handle enums")
	public void testPatternWithEnum() {
		NeoPattern p = entities.getPattern_EverythingBlue();
		var matches = p.getMatches();
		assertThat(matches.size(), is(1));
	}
}
