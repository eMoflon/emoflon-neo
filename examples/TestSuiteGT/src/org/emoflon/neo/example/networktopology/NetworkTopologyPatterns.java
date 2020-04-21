package org.emoflon.neo.example.networktopology;

import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.API_NetworkTopology;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NetworkTopologyPatterns extends ENeoTest {
	private API_NetworkTopology entities = new API_NetworkTopology(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI);
	
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
