package org.emoflon.neo.example.sokoban.emf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emoflon.neo.api.API_Src_Metamodels_SokobanLanguage;
import org.emoflon.neo.emf.EMFExporter;
import org.emoflon.neo.emf.EMFImporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TestEMFImportExport {

	@BeforeAll
	static void initEMF() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());		
	}
	
	@Test
	void testImportSimpleFamilies() throws IOException {
		var importer = new EMFImporter();
		
		var rs = new ResourceSetImpl();
		rs.getResource(URI.createFileURI("./resources/in/SimpleFamilies.ecore"), true);

		String expected = FileUtils.readFileToString(new File("./resources/expected/SimpleFamilies.msl"));
		assertEquals(expected, importer.generateEMSLSpecification(rs));
	}

	@Test
	void testExportSimpleFamilies() throws IOException {
		var metamodel = new API_Src_Metamodels_SokobanLanguage(null);
		
		var exporter = new EMFExporter(metamodel.getMetamodel_SokobanLanguage().eResource().getResourceSet(), "platform:/resource/TestSuite/out/", ".ecore");
		var rs = exporter.generateEMFModelsFromEMSL();
		for (var r : rs.getResources()) {
			r.save(null);
		}
	}
}