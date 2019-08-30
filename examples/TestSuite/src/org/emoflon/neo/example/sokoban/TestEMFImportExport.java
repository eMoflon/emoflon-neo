package org.emoflon.neo.example.sokoban;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.metamodels.API_SokobanLanguage;
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

		String expected = FileUtils.readFileToString(new File("./resources/expected/SimpleFamilies.msl"), Charset.defaultCharset());
		assertEquals(expected, importer.generateEMSLSpecification(rs));
	}

	@Test
	void testExportSimpleFamilies() throws IOException {
		var metamodel = new API_SokobanLanguage(API_Common.createBuilder(), API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI);
		
		var exporter = new EMFExporter(metamodel.getMetamodel_SokobanLanguage().eResource().getResourceSet(), "platform:/resource/TestSuite/out/", ".ecore");
		var rs = exporter.generateEMFModelsFromEMSL();
		for (var r : rs.getResources()) {
			r.save(null);
		}
	}
}