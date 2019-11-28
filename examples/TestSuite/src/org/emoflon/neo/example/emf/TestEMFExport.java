package org.emoflon.neo.example.emf;

import java.io.IOException;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.metamodels.API_SokobanLanguage;
import org.emoflon.neo.emf.EMFExporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TestEMFExport {

	@BeforeAll
	static void initEMF() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());		
	}

	@Test
	void testExportSimpleFamilies() throws IOException {
		var metamodel = new API_SokobanLanguage(API_Common.createBuilder(), API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI);
		
		var exporter = new EMFExporter(metamodel.getMetamodel_SokobanLanguage().eResource().getResourceSet(), "platform:/resource/TestSuite/resources/out/", ".ecore");
		var rs = exporter.generateEMFModelsFromEMSL();
		for (var r : rs.getResources()) {
			r.save(null);
		}
	}
}