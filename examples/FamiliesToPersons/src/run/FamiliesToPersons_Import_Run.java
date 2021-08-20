package run;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.emoflon.neo.api.familiestopersons.run.FamiliesToPersons_GEN_Run;
import org.emoflon.neo.emf.Neo4jImporter;
import org.emoflon.neo.neocore.ENeoUtil;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;

public class FamiliesToPersons_Import_Run {
	
	private static final Logger logger = Logger.getLogger(FamiliesToPersons_GEN_Run.class);

        /** arg0=FamiliesMM, arg1=PersonsMM, arg2=FamiliesModel, arg3=PersonsModel
        */
	public static void main(String[] pArgs) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		
		ResourceSet rs = ENeoUtil.createEMSLStandaloneResourceSet(".");
				
		loadMetamodel(rs, "./src/metamodels/Families.ecore");
		loadMetamodel(rs, "./src/metamodels/Persons.ecore");
		loadMetamodel(rs, "./src/metamodels/IBeXTGGFamiliesToPersons.ecore");
		
		loadModel(rs, "./src/models/Families.xmi", "Families");
		loadModel(rs, "./src/models/Persons.xmi", "Persons");
		
		new Neo4jImporter().importEMFModels(rs, "bolt://localhost:7687", "neo4j", "test");

	}
	
	private static void loadModel(ResourceSet rs, String uri, String label) {
		Resource resource = rs.getResource(URI.createURI(uri), true);
		resource.setURI(URI.createURI(label));
	}

	private static void loadMetamodel(ResourceSet rs, String uri) {
		var resource = rs.getResource(URI.createURI(uri), true);
		EPackage root = (EPackage) resource.getContents().get(0);
		resource.setURI(URI.createURI(root.getNsURI()));
	}

}

