package org.emoflon.neo.example.networktopology;

import org.emoflon.neo.api.networktopology.API_NetworkTopology;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NetworkTopologyPatterns extends ENeoTest {
	private API_NetworkTopology entities = new API_NetworkTopology(builder);
	
	@BeforeEach
	public void initDB() {
		initDB(entities.getModel_ExampleTopology());
	}
	
	@Test
	public void testPyramid() {
		expectMatches(entities.getPattern_Pyramid(), 1);
	}
	
	@Test
	public void testRectangle() {
		expectMatches(entities.getPattern_Rectangle(), 1);
	}
	
	@Test
	public void testTriangle() {
		expectMatches(entities.getPattern_Triangle(), 12);
	}
	
	@Test
	public void testPairs() {
		expectMatches(entities.getPattern_Pair(), 9);
	}
}
