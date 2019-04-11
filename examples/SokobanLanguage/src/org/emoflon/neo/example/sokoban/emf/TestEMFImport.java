package org.emoflon.neo.example.sokoban.emf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emoflon.neo.emf.EMFImporter;
import org.junit.jupiter.api.Test;

class TestEMFImport {

	@Test
	void testSimpleFamilies() throws IOException {
		var importer = new EMFImporter();
		
		var rs = new ResourceSetImpl();
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		rs.getResource(URI.createFileURI("./resources/in/SimpleFamilies.ecore"), true);

		String expected = FileUtils.readFileToString(new File("./resources/expected/SimpleFamilies.msl"));
		assertEquals(expected, importer.generateEMSLSpecification(rs));
	}

}