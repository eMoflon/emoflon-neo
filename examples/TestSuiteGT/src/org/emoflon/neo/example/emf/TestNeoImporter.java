package org.emoflon.neo.example.emf;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emoflon.neo.emf.Neo4jImporter;
import org.emoflon.neo.emsl.ui.internal.EmslActivator;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestNeoImporter extends ENeoTest {
	
	@BeforeAll
	static void initEMF() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
	}
	
	@Test
	public void testNeoImporter() {
		
		var importer = new Neo4jImporter();

		var rs = new ResourceSetImpl();
		rs.getResource(URI.createFileURI("./resources/in/metamodel/SimpleFamilies.ecore"), true);
		
//		String uri = EmslActivator.getInstance().getPreferenceStore().getString(EMSLUtil.P_URI);
//		String userName = EmslActivator.getInstance().getPreferenceStore().getString(EMSLUtil.P_USER);
//		String password = EmslActivator.getInstance().getPreferenceStore().getString(EMSLUtil.P_PASSWORD);
		
		String uri = "bolt://localhost:7687";
		String userName = "neo4j";
		String password = "test";
		
		importer.importEMFModels(rs, uri, userName, password);
	}
}
