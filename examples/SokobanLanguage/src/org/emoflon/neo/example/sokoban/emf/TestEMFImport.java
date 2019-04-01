package org.emoflon.neo.example.sokoban.emf;

import java.io.File;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emoflon.neo.emf.EMFImporter;
import org.junit.jupiter.api.Test;

class TestEMFImport {

	@Test
	void test() {
		var importer = new EMFImporter();

		var rs = new ResourceSetImpl();
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		rs.getResource(URI.createFileURI("./resources/in/SimpleFamilies.ecore"), true);
		
		var target = new File("./resources/out/Test.msl");
		importer.saveEMSLSpecification(rs, target);
	}

}
