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
		var noOfNodes = builder.noOfNodesInDatabase();
		var noOfEdges = builder.noOfEdgesInDatabase();
		var result = entities.getRule_CreateTs().rule().apply();
		var newNoOfNodes = builder.noOfNodesInDatabase();
		var newNoOfEdges = builder.noOfEdgesInDatabase();
		
		assertTrue(result.isPresent());
		
		assertTrue(entities.getConstraint_CorrectTyping().isSatisfied());
		
		assertEquals(5, newNoOfNodes - noOfNodes);
		assertEquals(5, newNoOfEdges - noOfEdges);
		
		assertTrue(entities.getConstraint_TestTyping().isSatisfied());
	}
}
