package org.emoflon.neo.example.typing;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.emoflon.neo.api.org.emoflon.neo.example.typing.API_TypingTest;
import org.emoflon.neo.emsl.util.FlattenerException;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TypingTest extends ENeoTest {
	private API_TypingTest entities = new API_TypingTest(builder);

	@BeforeEach
	public void initDB() {
		try {
			builder.exportEMSLEntityToNeo4j(entities.getMetamodel_T1());
			builder.exportEMSLEntityToNeo4j(entities.getMetamodel_T2());
			builder.exportEMSLEntityToNeo4j(entities.getMetamodel_T3());
			builder.exportEMSLEntityToNeo4j(entities.getMetamodel_T4());
			builder.exportEMSLEntityToNeo4j(entities.getMetamodel_T5());
		} catch (FlattenerException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testCorrectMetaTypes() {
		assertTrue(entities.getConstraint_TestTyping().isSatisfied());

		// Apply the rule creatings Ts
		var noOfNodes = builder.noOfNodesInDatabase();
		var noOfEdges = builder.noOfEdgesInDatabase();
		var result = entities.getRule_CreateTs().rule().apply();
		var newNoOfNodes = builder.noOfNodesInDatabase();
		var newNoOfEdges = builder.noOfEdgesInDatabase();

		// Was the rule applied?
		assertTrue(result.isPresent());

		// Was the current number of elements created?
		assertEquals(5, newNoOfNodes - noOfNodes);
		assertEquals(5, newNoOfEdges - noOfEdges);

		// Are types unique?
		assertTrue(entities.getConstraint_TestTyping().isSatisfied());

		// Does every T have a type?
		assertTrue(entities.getConstraint_CorrectTyping().isSatisfied());

		// Are Ts matched correctly?
		assertEquals(1, entities.getPattern_MatchT1().matcher().countMatches());
	}
}
