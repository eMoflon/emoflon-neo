package org.emoflon.neo.example.javatodocsle;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.emoflon.neo.emf.Neo4jImporter;
import org.junit.jupiter.api.Test;

public class EcoreTest {

	@Test
	public void testNeo4jImporter() {
		Neo4jImporter importer = new Neo4jImporter();
		
		var rs = new ResourceSetImpl();
		rs.createResource(URI.createFileURI("platform:/resource/TestSuiteTGG/ecore-test/src.xmi"));
		
		importer.importEMFModels(rs, "bolt://localhost:7687", "neo4j", "test");
	}
}
